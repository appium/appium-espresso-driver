import semver from 'semver';
import {TRACKED_MODULES} from './tracked-modules.mjs';

/**
 * @param {string | undefined} serverVersion
 * @param {string | null} appVersion
 * @returns {import('./types.mjs').VersionDiffKind}
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
 * @param {Record<string, string>} serverVersions
 * @param {Record<string, string[]>} appVersions
 * @param {{proguardLikely?: boolean, minifyEnabled?: boolean | null, detectionSource?: 'apk' | 'project'}} context
 * @returns {import('./types.mjs').ComparisonReport}
 */
export function buildComparisonReport(serverVersions, appVersions, context = {}) {
  const proguardLikely = Boolean(context.proguardLikely);
  const minifyEnabled = context.minifyEnabled ?? null;
  const detectionSource = context.detectionSource ?? 'project';
  /** @type {import('./types.mjs').ModuleComparison[]} */
  const modules = [];

  for (const mod of TRACKED_MODULES) {
    const serverVersion = serverVersions[mod.id];
    const appCandidates = appVersions[mod.id] ?? [];
    const appVersion = appCandidates[0] ?? null;

    if (mod.testOnly) {
      modules.push({
        ...mod,
        serverVersion: serverVersion ?? null,
        appVersions: appCandidates,
        appVersion,
        diff: appVersion ? 'present' : 'absent',
        recommendation: buildTestOnlyRecommendation(mod, appCandidates),
      });
      continue;
    }

    const diff = compareModuleVersions(serverVersion, appVersion);
    modules.push({
      ...mod,
      serverVersion: serverVersion ?? null,
      appVersions: appCandidates,
      appVersion,
      diff,
      recommendation: buildVersionRecommendation(mod, diff, appVersion, serverVersion, {
        proguardLikely,
        detectionSource,
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
 * @param {string[]} appVersions
 * @returns {string}
 */
function formatDetectedVersions(appVersions) {
  return appVersions.length ? appVersions.join(', ') : 'detected';
}

/**
 * @param {import('./types.mjs').TrackedModule} mod
 * @param {string[]} appVersions
 * @returns {import('./types.mjs').Recommendation}
 */
function buildTestOnlyRecommendation(mod, appVersions) {
  if (appVersions.length) {
    const versions = formatDetectedVersions(appVersions);
    return {
      level: 'warning',
      message:
        `Detected ${mod.label} (${versions}) in the application under test. ` +
        'The main app should not be built as an instrumented-test artifact and is not expected to ' +
        'bundle Espresso, AndroidX Test, or UiAutomator dependencies; doing so ' +
        'can cause classpath conflicts with the Espresso server.',
    };
  }
  return {level: 'ok', message: 'Not detected in the app; no action needed.'};
}

/**
 * @param {import('./types.mjs').TrackedModule} mod
 * @param {string} appVersion
 * @returns {import('./types.mjs').Recommendation['espressoBuildConfig']}
 */
function toolsVersionsOverride(mod, appVersion) {
  if (!mod.toolsVersionKey) {
    return undefined;
  }
  return {toolsVersions: {[mod.toolsVersionKey]: appVersion}};
}

/**
 * @param {import('./types.mjs').TrackedModule} mod
 * @param {import('./types.mjs').VersionDiffKind} diff
 * @param {string | null} appVersion
 * @param {string | undefined} serverVersion
 * @param {{proguardLikely: boolean, detectionSource: 'apk' | 'project'}} ctx
 * @returns {import('./types.mjs').Recommendation}
 */
function buildVersionRecommendation(mod, diff, appVersion, serverVersion, ctx) {
  if (!serverVersion) {
    return {level: 'info', message: 'No bundled server version found.'};
  }
  if (ctx.proguardLikely && !appVersion) {
    return {
      level: 'info',
      message:
        'Could not read this module from the APK (common with R8/ProGuard). Use --app with Gradle sources or align manually.',
    };
  }
  if (!appVersion) {
    if (mod.id === 'compose' && ctx.detectionSource === 'project') {
      return {
        level: 'suggestion',
        message:
          'No Compose UI Test dependencies detected. Consider `"composeSupport": false` in espressoBuildConfig if the AUT is not Compose-based.',
        espressoBuildConfig: {composeSupport: false},
      };
    }
    return {
      level: 'info',
      message:
        ctx.detectionSource === 'apk'
          ? 'Not detected in APK scan. If the app uses this library, pass the Gradle project root or ensure AGP embedded META-INF/*.version files are present.'
          : 'Not detected in the app; no action needed.',
    };
  }
  if (diff === 'equal' || diff === 'patch') {
    return {level: 'ok', message: 'Versions are compatible.'};
  }
  if (diff === 'minor') {
    return {
      level: 'suggestion',
      message: `Minor version drift. Align the Espresso server via toolsVersions.${mod.toolsVersionKey}.`,
      espressoBuildConfig: toolsVersionsOverride(mod, appVersion),
    };
  }
  if (diff === 'major' || diff === 'unknown') {
    const serverIsNewer = semver.valid(serverVersion) && semver.valid(appVersion)
      ? semver.gt(serverVersion, appVersion)
      : false;
    if (serverIsNewer) {
      return {
        level: 'warning',
        message:
          'Large version gap: the Espresso server is newer than the app. Prefer updating the app’s AndroidX/Compose/Espresso dependencies to match, or override server versions via espressoBuildConfig (may be unstable).',
        espressoBuildConfig: toolsVersionsOverride(mod, appVersion),
        preferAppUpdate: true,
      };
    }
    return {
      level: 'warning',
      message:
        'Large version gap: the app is ahead of the Espresso server defaults. Set matching versions in espressoBuildConfig.toolsVersions before rebuilding the server.',
      espressoBuildConfig: toolsVersionsOverride(mod, appVersion),
      preferAppUpdate: false,
    };
  }
  return {level: 'ok', message: 'Versions are compatible.'};
}

/**
 * @param {import('./types.mjs').ModuleComparison[]} modules
 * @param {{proguardLikely: boolean, minifyEnabled: boolean | null}} ctx
 */
function summarizeReport(modules, ctx) {
  const testPresence = modules.filter((m) => m.testOnly && m.recommendation.level === 'warning');
  const warnings = modules.filter((m) => !m.testOnly && m.recommendation.level === 'warning');
  const suggestions = modules.filter((m) => m.recommendation.level === 'suggestion');
  if (ctx.proguardLikely) {
    return 'APK appears minified/obfuscated; dependency versions from the APK may be incomplete. Espresso server defaults are listed below.';
  }
  if (testPresence.length) {
    return `${testPresence.length} test/instrumentation librar${testPresence.length === 1 ? 'y' : 'ies'} detected in the application under test — review warnings below.`;
  }
  if (warnings.length) {
    return `${warnings.length} module(s) have a large version mismatch — review warnings below.`;
  }
  if (suggestions.length) {
    return `${suggestions.length} module(s) have minor drift — optional espressoBuildConfig alignment suggested.`;
  }
  return 'No significant dependency conflicts detected.';
}
