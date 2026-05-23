import {fs, tempDir, zip} from 'appium/support.js';
import path from 'node:path';
import semver from 'semver';

/** @typedef {'equal' | 'patch' | 'minor' | 'major' | 'unknown'} VersionDiffKind */

/**
 * @typedef {Object} Recommendation
 * @property {'ok' | 'info' | 'suggestion' | 'warning'} level
 * @property {string} message
 * @property {Record<string, unknown>} [espressoBuildConfig]
 * @property {boolean} [preferAppUpdate]
 */

/**
 * @typedef {TrackedModule & {
 *   serverVersion: string | null,
 *   appVersions: string[],
 *   appVersion: string | null,
 *   diff: VersionDiffKind,
 *   recommendation: Recommendation,
 * }} ModuleComparison
 */

/**
 * @typedef {Object} ComparisonReport
 * @property {boolean} proguardLikely
 * @property {boolean | null} minifyEnabled
 * @property {ModuleComparison[]} modules
 * @property {string} summary
 */

/**
 * @typedef {Object} TrackedModule
 * @property {string} id
 * @property {string} label
 * @property {string | null} toolsVersionKey
 * @property {string} catalogKey
 * @property {string} gradleProperty
 * @property {RegExp[]} patterns
 */

/** Modules the Espresso server ships and that commonly conflict with the AUT. */
export const TRACKED_MODULES = [
  {
    id: 'compose',
    label: 'Jetpack Compose UI Test',
    toolsVersionKey: 'composeVersion',
    catalogKey: 'composeUiTest',
    gradleProperty: 'appiumComposeVersion',
    patterns: [
      /composeUiTest\s*=\s*["']([^"']+)["']/gi,
      /compose-ui-test[^:]*:([\d.]+)/gi,
      /compose\.ui:ui-test[^:]*:([\d.]+)/gi,
      /["']androidx\.compose\.ui:ui-test(?:-junit4)?["'][^:]*:([\d.]+)/gi,
      /composeBom\s*=\s*["']([^"']+)["']/gi,
      /compose-bom:([\d.]+)/gi,
      /androidx\.compose:compose-bom:([\d.]+)/gi,
    ],
  },
  {
    id: 'espresso',
    label: 'Espresso',
    toolsVersionKey: 'espressoVersion',
    catalogKey: 'espresso',
    gradleProperty: 'appiumEspressoVersion',
    patterns: [
      /\bespresso\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test\.espresso:espresso-[\w-]+:([\d.]+)/gi,
      /espresso-core:([\d.]+)/gi,
    ],
  },
  {
    id: 'annotation',
    label: 'AndroidX Annotation',
    toolsVersionKey: 'annotationVersion',
    catalogKey: 'annotation',
    gradleProperty: 'appiumAnnotationVersion',
    patterns: [
      /\bannotation\s*=\s*["']([^"']+)["']/gi,
      /androidx\.annotation:annotation:([\d.]+)/gi,
    ],
  },
  {
    id: 'androidxTest',
    label: 'AndroidX Test',
    toolsVersionKey: null,
    catalogKey: 'androidxTest',
    gradleProperty: 'appiumAndroidxTestVersion',
    patterns: [
      /androidxTest\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test:(?:core|runner|rules|ext):[\w-]*:([\d.]+)/gi,
    ],
  },
  {
    id: 'uiautomator',
    label: 'UiAutomator',
    toolsVersionKey: null,
    catalogKey: 'uiautomator',
    gradleProperty: 'appiumUiAutomatorVersion',
    patterns: [
      /\buiautomator\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test\.uiautomator:uiautomator:([\d.]+)/gi,
    ],
  },
  {
    id: 'kotlin',
    label: 'Kotlin',
    toolsVersionKey: 'kotlin',
    catalogKey: 'kotlin',
    gradleProperty: 'appiumKotlin',
    patterns: [
      /\bkotlin\s*=\s*["']([^"']+)["']/gi,
      /org\.jetbrains\.kotlin:[\w-]+:([\d.]+)/gi,
    ],
  },
];

/**
 * @param {string} espressoServerRoot
 * @returns {Promise<Record<string, string>>}
 */
export async function loadEspressoServerVersions(espressoServerRoot) {
  const tomlPath = path.join(espressoServerRoot, 'gradle', 'libs.versions.toml');
  const text = await fs.readFile(tomlPath, 'utf8');
  const versions = parseVersionsToml(text);
  /** @type {Record<string, string>} */
  const result = {};
  for (const mod of TRACKED_MODULES) {
    const version = versions[mod.catalogKey];
    if (version) {
      result[mod.id] = version;
    }
  }
  return result;
}

/**
 * @param {string} text
 * @returns {Record<string, string>}
 */
export function parseVersionsToml(text) {
  /** @type {Record<string, string>} */
  const versions = {};
  let inVersionsSection = false;
  for (const line of text.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (trimmed === '[versions]') {
      inVersionsSection = true;
      continue;
    }
    if (trimmed.startsWith('[') && trimmed !== '[versions]') {
      inVersionsSection = false;
      continue;
    }
    if (!inVersionsSection || !trimmed || trimmed.startsWith('#')) {
      continue;
    }
    const match = trimmed.match(/^([A-Za-z0-9_.-]+)\s*=\s*["']([^"']+)["']/);
    if (match) {
      versions[match[1]] = match[2];
    }
  }
  return versions;
}

/**
 * @param {string} projectRoot
 * @returns {Promise<{versions: Record<string, string[]>, minifyEnabled: boolean | null, sources: string[]}>}
 */
export async function collectAppVersionsFromProject(projectRoot) {
  const gradleFiles = await findGradleFiles(projectRoot);
  /** @type {Record<string, Set<string>>} */
  const found = Object.fromEntries(TRACKED_MODULES.map((m) => [m.id, new Set()]));
  let minifyEnabled = null;

  for (const filePath of gradleFiles) {
    const text = await fs.readFile(filePath, 'utf8');
    const rel = path.relative(projectRoot, filePath);
    for (const mod of TRACKED_MODULES) {
      for (const pattern of mod.patterns) {
        pattern.lastIndex = 0;
        let match;
        while ((match = pattern.exec(text)) !== null) {
          const version = normalizeVersion(match[1]);
          if (version) {
            found[mod.id].add(version);
          }
        }
      }
    }
    if (/\bisMinifyEnabled\s*=\s*true\b/.test(text) || /\bminifyEnabled\s+true\b/.test(text)) {
      minifyEnabled = true;
    }
    if (/\bisMinifyEnabled\s*=\s*false\b/.test(text) || /\bminifyEnabled\s+false\b/.test(text)) {
      if (minifyEnabled === null) {
        minifyEnabled = false;
      }
    }
    if (rel.endsWith('libs.versions.toml')) {
      const tomlVersions = parseVersionsToml(text);
      for (const mod of TRACKED_MODULES) {
        const v = tomlVersions[mod.catalogKey];
        if (v) {
          const normalized = normalizeVersion(v);
          if (normalized) {
            found[mod.id].add(normalized);
          }
        }
      }
    }
  }

  /** @type {Record<string, string[]>} */
  const versions = {};
  for (const [id, set] of Object.entries(found)) {
    versions[id] = [...set].sort(compareVersionsDesc);
  }
  return {versions, minifyEnabled, sources: gradleFiles.map((f) => path.relative(projectRoot, f))};
}

/**
 * @param {string} apkPath
 * @returns {Promise<{versions: Record<string, string[]>, proguardLikely: boolean, sources: string[]}>}
 */
export async function collectAppVersionsFromApk(apkPath) {
  const extractDir = await tempDir.openDir();
  try {
    await extractApk(apkPath, extractDir);
    const dexStrings = await readAllDexStrings(extractDir);
    const metaStrings = await collectMetaInfStrings(extractDir);

    /** @type {Record<string, Set<string>>} */
    const found = Object.fromEntries(TRACKED_MODULES.map((m) => [m.id, new Set()]));
    const corpus = `${dexStrings}\n${metaStrings}`;

    for (const mod of TRACKED_MODULES) {
      for (const pattern of mod.patterns) {
        pattern.lastIndex = 0;
        let match;
        while ((match = pattern.exec(corpus)) !== null) {
          const version = normalizeVersion(match[1]);
          if (version) {
            found[mod.id].add(version);
          }
        }
      }
    }

    const proguardLikely = detectProguardLikely(dexStrings, corpus);

    /** @type {Record<string, string[]>} */
    const versions = {};
    for (const [id, set] of Object.entries(found)) {
      versions[id] = [...set].sort(compareVersionsDesc);
    }
    return {
      versions,
      proguardLikely,
      sources: ['APK DEX/META-INF scan'],
    };
  } finally {
    await fs.rimraf(extractDir);
  }
}

/**
 * @param {Record<string, string>} serverVersions
 * @param {Record<string, string[]>} appVersions
 * @param {{proguardLikely?: boolean, minifyEnabled?: boolean | null, detectionSource?: 'apk' | 'project'}} context
 * @returns {ComparisonReport}
 */
export function buildComparisonReport(serverVersions, appVersions, context = {}) {
  const proguardLikely = Boolean(context.proguardLikely);
  const minifyEnabled = context.minifyEnabled ?? null;
  /** @type {ModuleComparison[]} */
  const modules = [];

  for (const mod of TRACKED_MODULES) {
    const serverVersion = serverVersions[mod.id];
    const appCandidates = appVersions[mod.id] ?? [];
    const appVersion = appCandidates[0] ?? null;
    const diff = compareModuleVersions(serverVersion, appVersion);
    modules.push({
      ...mod,
      serverVersion: serverVersion ?? null,
      appVersions: appCandidates,
      appVersion,
      diff,
      recommendation: buildRecommendation(mod, diff, appVersion, serverVersion, {
        proguardLikely,
        minifyEnabled,
        appHasCompose: Boolean(appVersions.compose?.length),
        detectionSource: context.detectionSource ?? 'project',
      }),
    });
  }

  return {
    proguardLikely,
    minifyEnabled,
    modules,
    summary: summarizeReport(modules, {proguardLikely, minifyEnabled}),
  };
}

/**
 * @param {string | undefined} serverVersion
 * @param {string | null} appVersion
 * @returns {VersionDiffKind}
 */
export function compareModuleVersions(serverVersion, appVersion) {
  if (!serverVersion || !appVersion) {
    return 'unknown';
  }
  const s = semver.coerce(serverVersion);
  const a = semver.coerce(appVersion);
  if (!s || !a) {
    return serverVersion === appVersion ? 'equal' : 'unknown';
  }
  if (semver.eq(s, a)) {
    return 'equal';
  }
  if (semver.major(s) !== semver.major(a)) {
    return 'major';
  }
  if (semver.minor(s) !== semver.minor(a)) {
    return 'minor';
  }
  return 'patch';
}

/**
 * @param {ComparisonReport} report
 * @param {{json?: boolean, compact?: boolean}} [opts]
 */
export function formatReport(report, opts = {}) {
  if (opts.json) {
    return JSON.stringify(report, null, 2);
  }
  const lines = [];
  if (!opts.compact) {
    lines.push('Espresso dependency compatibility report');
    lines.push('======================================');
    lines.push('');
  }
  lines.push(report.summary);
  lines.push('');

  if (report.proguardLikely || report.minifyEnabled) {
    lines.push('⚠ ProGuard / R8');
    if (report.minifyEnabled === true) {
      lines.push('  Gradle reports minifyEnabled=true for this project.');
    }
    if (report.proguardLikely) {
      lines.push(
        '  The APK looks obfuscated/minified. Dependency versions extracted from the APK may be incomplete.',
      );
    }
    lines.push('');
    lines.push('Espresso server default module versions (from driver libs.versions.toml):');
    for (const mod of report.modules) {
      if (mod.serverVersion) {
        lines.push(`  ${mod.label}: ${mod.serverVersion}`);
      }
    }
    lines.push('');
  }

  for (const mod of report.modules) {
    lines.push(`${mod.label} (${mod.id})`);
    lines.push(`  Server: ${mod.serverVersion ?? 'n/a'}`);
  if (mod.appVersions.length) {
      lines.push(`  App:    ${mod.appVersions.join(', ')} (using ${mod.appVersion} for comparison)`);
    } else {
      lines.push('  App:    not detected');
    }
    lines.push(`  Diff:   ${mod.diff}`);
    lines.push(`  → ${mod.recommendation.message}`);
    if (mod.recommendation.espressoBuildConfig) {
      lines.push(
        `  Suggested espressoBuildConfig fragment:\n${JSON.stringify(mod.recommendation.espressoBuildConfig, null, 4)
          .split('\n')
          .map((l) => `    ${l}`)
          .join('\n')}`,
      );
    }
    lines.push('');
  }

  const mergedConfig = mergeEspressoBuildConfigSuggestions(report.modules);
  if (Object.keys(mergedConfig).length) {
    lines.push('Merged espressoBuildConfig suggestion (combine with your existing config):');
    lines.push(JSON.stringify(mergedConfig, null, 2));
    lines.push('');
  }

  return lines.join('\n');
}

/**
 * @param {string} dexStrings
 * @param {string} corpus
 * @returns {boolean}
 */
function detectProguardLikely(dexStrings, corpus) {
  const androidxHits = (corpus.match(/androidx\//g) ?? []).length;
  const hasR8 = /\bR8\b/.test(corpus) || /proguard/i.test(corpus);
  const fewReadableAndroidx = androidxHits < 3 && dexStrings.length > 100_000;
  const obfuscatedKotlin = (dexStrings.match(/\bL[a-z]{1,2}\/[a-z]{1,2};/g) ?? []).length > 500;
  return (hasR8 && fewReadableAndroidx) || (obfuscatedKotlin && fewReadableAndroidx);
}

/**
 * @param {TrackedModule} mod
 * @param {VersionDiffKind} diff
 * @param {string | null} appVersion
 * @param {string | undefined} serverVersion
 * @param {{proguardLikely: boolean, minifyEnabled: boolean | null, appHasCompose: boolean, detectionSource: 'apk' | 'project'}} ctx
 */
function buildRecommendation(mod, diff, appVersion, serverVersion, ctx) {
  if (!serverVersion) {
    return /** @type {Recommendation} */ ({level: 'info', message: 'No bundled server version found.'});
  }
  if (ctx.proguardLikely && !appVersion) {
    return /** @type {Recommendation} */ ({
      level: 'info',
      message:
        'Could not read this module from the APK (common with R8/ProGuard). Use --app with Gradle sources or align manually.',
    });
  }
  if (!appVersion) {
    if (mod.id === 'compose' && ctx.detectionSource === 'project' && !ctx.appHasCompose) {
      return /** @type {Recommendation} */ ({
        level: 'suggestion',
        message:
          'App does not declare Compose test dependencies. Consider `"composeSupport": false` in espressoBuildConfig if the AUT is not Compose-based.',
        espressoBuildConfig: {composeSupport: false},
      });
    }
    if (ctx.detectionSource === 'apk') {
      return /** @type {Recommendation} */ ({
        level: 'info',
        message:
          'Not detected in APK bytecode scan. Prefer `--app` with the Gradle project root for accurate Compose/Espresso versions.',
      });
    }
    return /** @type {Recommendation} */ ({level: 'ok', message: 'Not detected in the app; no action needed.'});
  }
  if (diff === 'equal' || diff === 'patch') {
    return /** @type {Recommendation} */ ({level: 'ok', message: 'Versions are compatible.'});
  }
  if (diff === 'minor') {
    return /** @type {Recommendation} */ ({
      level: 'suggestion',
      message: `Minor version drift. Align the Espresso server via toolsVersions.${mod.toolsVersionKey ?? mod.gradleProperty}.`,
      espressoBuildConfig: mod.toolsVersionKey
        ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
        : {gradleProperty: {[mod.gradleProperty]: appVersion}},
    });
  }
  if (diff === 'major' || diff === 'unknown') {
    const serverIsNewer = semver.valid(serverVersion) && semver.valid(appVersion)
      ? semver.gt(serverVersion, appVersion)
      : false;
    if (serverIsNewer) {
      return /** @type {Recommendation} */ ({
        level: 'warning',
        message:
          'Large version gap: the Espresso server is newer than the app. Prefer updating the app’s AndroidX/Compose/Espresso dependencies to match, or override server versions via espressoBuildConfig (may be unstable).',
        espressoBuildConfig: mod.toolsVersionKey
          ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
          : {gradleProperty: {[mod.gradleProperty]: appVersion}},
        preferAppUpdate: true,
      });
    }
    return /** @type {Recommendation} */ ({
      level: 'warning',
      message:
        'Large version gap: the app is ahead of the Espresso server defaults. Set matching versions in espressoBuildConfig.toolsVersions before rebuilding the server.',
      espressoBuildConfig: mod.toolsVersionKey
        ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
        : {gradleProperty: {[mod.gradleProperty]: appVersion}},
      preferAppUpdate: false,
    });
  }
  return /** @type {Recommendation} */ ({level: 'ok', message: 'Versions are compatible.'});
}

/**
 * @param {ModuleComparison[]} modules
 */
/**
 * @param {ModuleComparison[]} modules
 * @param {{proguardLikely: boolean, minifyEnabled: boolean | null}} ctx
 */
function summarizeReport(modules, ctx) {
  const warnings = modules.filter((m) => m.recommendation.level === 'warning');
  const suggestions = modules.filter((m) => m.recommendation.level === 'suggestion');
  if (ctx.proguardLikely) {
    return 'APK appears minified/obfuscated; dependency versions from the APK may be incomplete. Espresso server defaults are listed below.';
  }
  if (warnings.length) {
    return `${warnings.length} module(s) have a large version mismatch — review warnings below.`;
  }
  if (suggestions.length) {
    return `${suggestions.length} module(s) have minor drift — optional espressoBuildConfig alignment suggested.`;
  }
  return 'No significant dependency conflicts detected.';
}

/**
 * @param {ModuleComparison[]} modules
 */
function mergeEspressoBuildConfigSuggestions(modules) {
  /** @type {Record<string, unknown>} */
  const config = {};
  /** @type {Record<string, string>} */
  const toolsVersions = {};
  for (const mod of modules) {
    const fragment = mod.recommendation.espressoBuildConfig;
    if (!fragment) {
      continue;
    }
    if (fragment.composeSupport === false) {
      config.composeSupport = false;
    }
    if (fragment.toolsVersions) {
      Object.assign(toolsVersions, fragment.toolsVersions);
    }
  }
  if (Object.keys(toolsVersions).length) {
    config.toolsVersions = toolsVersions;
  }
  return config;
}

/**
 * @param {string} root
 * @param {number} [maxDepth]
 * @returns {Promise<string[]>}
 */
async function findGradleFiles(root, maxDepth = 6) {
  const matches = await fs.glob(
    '**/{build.gradle,build.gradle.kts,gradle.properties,libs.versions.toml}',
    {
      cwd: root,
      absolute: true,
      ignore: ['**/node_modules/**', '**/.git/**', '**/build/**'],
    },
  );
  return matches.filter((filePath) => {
    const parts = path.relative(root, filePath).split(path.sep).filter(Boolean);
    return parts.length - 1 <= maxDepth;
  });
}

/**
 * @param {string} apkPath
 * @param {string} destDir
 */
async function extractApk(apkPath, destDir) {
  try {
    await zip.extractAllTo(apkPath, destDir);
  } catch (err) {
    throw new Error(`Failed to extract APK. Use --app with a Gradle project instead.`, {cause: err});
  }
}

/**
 * @param {string} extractRoot
 * @returns {Promise<string>}
 */
async function readAllDexStrings(extractRoot) {
  /** @type {string[]} */
  const chunks = [];
  let entries;
  try {
    entries = await fs.readdir(extractRoot);
  } catch {
    return '';
  }
  for (const name of entries) {
    if (!/^classes\d*\.dex$/i.test(name)) {
      continue;
    }
    try {
      const buf = await fs.readFile(path.join(extractRoot, name));
      chunks.push(buf.toString('latin1'));
    } catch {
      // ignore
    }
  }
  return chunks.join('\n');
}

/**
 * @param {string} root
 * @returns {Promise<string>}
 */
async function collectMetaInfStrings(root) {
  const metaDir = path.join(root, 'META-INF');
  /** @type {string[]} */
  const chunks = [];
  /** @param {string} dir */
  async function walkMeta(dir) {
    let entries;
    try {
      entries = await fs.readdir(dir, {withFileTypes: true});
    } catch {
      return;
    }
    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        await walkMeta(full);
      } else if (/\.(properties|version|kotlin_module|txt)$/i.test(entry.name)) {
        try {
          chunks.push(await fs.readFile(full, 'utf8'));
        } catch {
          // ignore binary
        }
      }
    }
  }
  await walkMeta(metaDir);
  return chunks.join('\n');
}

/**
 * @param {string | undefined} raw
 * @returns {string | null}
 */
function normalizeVersion(raw) {
  if (!raw) {
    return null;
  }
  return semver.coerce(String(raw).trim(), {includePrerelease: true})?.version ?? null;
}

/**
 * @param {string} a
 * @param {string} b
 */
function compareVersionsDesc(a, b) {
  const sa = semver.coerce(a);
  const sb = semver.coerce(b);
  if (sa && sb) {
    return semver.rcompare(sa, sb);
  }
  return b.localeCompare(a);
}
