import {fs} from 'appium/support.js';
import {ADB} from 'appium-adb';
import path from 'node:path';
import semver from 'semver';
import {
  buildComparisonReport,
  collectAppVersionsFromApk,
  collectAppVersionsFromProject,
  formatReport as formatDependencyReport,
  loadEspressoServerVersions,
} from './dependency-versions.mjs';

/** @typedef {'pass' | 'warn' | 'fail' | 'info' | 'skip'} CheckStatus */

/**
 * Driver and espresso-server defaults loaded for dependency and static checks.
 * @typedef {Object} EspressoServerDefaults
 * @property {string} driverVersion
 * @property {string | null} compileSdk
 * @property {string | null} minSdk
 * @property {Record<string, string>} versions
 */

/**
 * @typedef {Object} DiagnosticCheck
 * @property {string} id
 * @property {string} title
 * @property {CheckStatus} status
 * @property {string} message
 * @property {Record<string, unknown>} [espressoBuildConfig]
 * @property {string} [docRef]
 */

/**
 * @typedef {Object} AppInput
 * @property {'apk' | 'project'} kind
 * @property {string} path
 * @property {Record<string, string[]>} versions
 * @property {boolean} proguardLikely
 * @property {boolean | null} minifyEnabled
 * @property {string[]} sources
 * @property {string} [gradleCorpus]
 * @property {string[]} [manifestPaths]
 * @property {boolean | null} [apkHasInternetPermission]
 */

/**
 * @param {string} espressoServerRoot
 * @param {string} driverRoot
 * @returns {Promise<EspressoServerDefaults>}
 */
export async function loadEspressoServerDefaults(espressoServerRoot, driverRoot) {
  const [versions, gradlePropsText, pkgText] = await Promise.all([
    loadEspressoServerVersions(espressoServerRoot),
    fs.readFile(path.join(espressoServerRoot, 'gradle.properties'), 'utf8'),
    fs.readFile(path.join(driverRoot, 'package.json'), 'utf8'),
  ]);
  const gradleProps = parseGradleProperties(gradlePropsText);
  const driverVersion = JSON.parse(pkgText).version;
  return {
    driverVersion,
    compileSdk: gradleProps.appiumCompileSdk ?? null,
    minSdk: gradleProps.appiumMinSdk ?? null,
    versions,
  };
}

/**
 * @param {AppInput} appInput
 * @param {EspressoServerDefaults} serverDefaults
 */
export async function runDiagnosis(appInput, serverDefaults) {
  const dependencyReport = buildComparisonReport(serverDefaults.versions, appInput.versions, {
    proguardLikely: appInput.proguardLikely,
    minifyEnabled: appInput.minifyEnabled,
    detectionSource: appInput.kind === 'apk' ? 'apk' : 'project',
  });

  /** @type {DiagnosticCheck[]} */
  const checks = [];

  checks.push(checkPrecompileInputKind(appInput));
  checks.push(...(await checkInternetPermission(appInput)));
  checks.push(...checkObfuscation(appInput, serverDefaults));
  checks.push(...checkAndroidX(appInput));
  checks.push(...checkInitializationProvider(appInput));
  checks.push(...checkCompileSdk(appInput, serverDefaults, dependencyReport));
  checks.push(...checkLifecycleExtensionsPin(appInput));
  checks.push(...checkEspressoServerEmbedding(appInput));
  checks.push(...mapDependencyChecks(dependencyReport, appInput));

  const failCount = checks.filter((c) => c.status === 'fail').length;
  const warnCount = checks.filter((c) => c.status === 'warn').length;
  const ready = failCount === 0 && appInput.kind === 'project';

  let summary;
  if (appInput.kind === 'apk') {
    summary =
      failCount > 0
        ? `APK scan found ${failCount} blocking issue(s). Precompile readiness requires a Gradle project path.`
        : 'APK scan only — re-run with the Gradle project root to confirm precompile readiness.';
  } else if (ready) {
    summary =
      warnCount > 0
        ? `Ready for precompile with ${warnCount} warning(s) to review.`
        : 'Ready for precompile into the Espresso driver (library / androidTest module).';
  } else {
    summary = `Not ready for precompile (${failCount} blocking issue(s), ${warnCount} warning(s)).`;
  }

  return {
    ready,
    failCount,
    warnCount,
    checks,
    dependencyReport,
    serverDefaults,
    input: appInput,
    mergedEspressoBuildConfig: mergeAllEspressoBuildConfig(checks, dependencyReport),
    summary,
  };
}

/**
 * @param {Awaited<ReturnType<typeof runDiagnosis>>} report
 */
export function formatDiagnosisReport(report) {
  const lines = [];
  lines.push('Espresso precompile readiness diagnosis');
  lines.push('========================================');
  lines.push('');
  const verdict = report.ready ? 'READY' : report.input.kind === 'apk' ? 'INCOMPLETE' : 'NOT READY';
  lines.push(`Verdict: ${verdict} — ${report.summary}`);
  lines.push('');

  const statusIcon = /** @param {CheckStatus} s */ (s) => {
    if (s === 'pass') {return '[PASS]';}
    if (s === 'warn') {return '[WARN]';}
    if (s === 'fail') {return '[FAIL]';}
    if (s === 'skip') {return '[SKIP]';}
    return '[INFO]';
  };

  lines.push('Checks');
  lines.push('------');
  for (const check of report.checks) {
    lines.push(`${statusIcon(check.status)} ${check.title}`);
    lines.push(`       ${check.message}`);
    if (check.docRef) {
      lines.push(`       See: ${check.docRef}`);
    }
    lines.push('');
  }

  lines.push('Dependency alignment (Espresso server vs AUT)');
  lines.push('---------------------------------------------');
  lines.push(formatDependencyReport(report.dependencyReport, {json: false, compact: true}));
  lines.push('');

  if (Object.keys(report.mergedEspressoBuildConfig).length) {
    lines.push('Merged espressoBuildConfig (for dynamic server builds, if not precompiling):');
    lines.push(JSON.stringify(report.mergedEspressoBuildConfig, null, 2));
    lines.push('');
  }

  if (report.ready) {
    lines.push('Next: embed io.appium.espressoserver:library, build :app:assembleDebug :<testModule>:assembleDebug,');
    lines.push('then run sessions with appium:skipServerInstallation=true (see docs/as-library.md).');
  }

  return lines.join('\n');
}

/**
 * @param {string} projectRoot
 * @returns {Promise<AppInput>}
 */
export async function collectAppInputFromProject(projectRoot) {
  const {versions, minifyEnabled, sources} = await collectAppVersionsFromProject(projectRoot);
  const gradleFiles = sources.map((rel) => path.join(projectRoot, rel));
  const gradleCorpus = (
    await Promise.all(gradleFiles.map((f) => fs.readFile(f, 'utf8').catch(() => '')))
  ).join('\n');
  const manifestPaths = await findManifestFiles(projectRoot);
  const manifestTexts = await Promise.all(manifestPaths.map((f) => fs.readFile(f, 'utf8').catch(() => '')));

  return {
    kind: 'project',
    path: projectRoot,
    versions,
    proguardLikely: false,
    minifyEnabled,
    sources,
    gradleCorpus,
    manifestPaths: manifestTexts,
    apkHasInternetPermission: null,
  };
}

/**
 * @param {string} apkPath
 * @returns {Promise<AppInput>}
 */
export async function collectAppInputFromApk(apkPath) {
  const {versions, proguardLikely, sources} = await collectAppVersionsFromApk(apkPath);
  const apkHasInternetPermission = await detectApkInternetPermission(apkPath);
  return {
    kind: 'apk',
    path: apkPath,
    versions,
    proguardLikely,
    minifyEnabled: null,
    sources,
    apkHasInternetPermission,
  };
}

/**
 * @param {import('./dependency-versions.mjs').ComparisonReport} dependencyReport
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck[]}
 */
function mapDependencyChecks(dependencyReport, appInput) {
  return dependencyReport.modules
    .filter((mod) => mod.appVersion || mod.recommendation.level !== 'ok')
    .map((mod) => {
      const level = mod.recommendation.level;
      /** @type {CheckStatus} */
      let status = 'pass';
      if (level === 'warning') {
        status = 'fail';
      } else if (level === 'suggestion') {
        status = 'warn';
      } else if (level === 'info') {
        status = appInput.kind === 'apk' ? 'info' : 'pass';
      } else if (mod.appVersion && (mod.diff === 'equal' || mod.diff === 'patch')) {
        status = 'pass';
      }

      return /** @type {DiagnosticCheck} */ ({
        id: `dependency-${mod.id}`,
        title: `${mod.label} version`,
        status,
        message: mod.recommendation.message,
        espressoBuildConfig: mod.recommendation.espressoBuildConfig,
      });
    });
}

/**
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck}
 */
function checkPrecompileInputKind(appInput) {
  if (appInput.kind === 'project') {
    return {
      id: 'input-gradle-project',
      title: 'Gradle project',
      status: 'pass',
      message: 'Gradle sources detected — static checks can inspect manifests and build files.',
    };
  }
  return {
    id: 'input-gradle-project',
    title: 'Gradle project',
    status: 'warn',
    message:
      'APK-only input: precompile requires a Gradle module with io.appium.espressoserver:library. Re-run with the Android project root for full diagnosis.',
    docRef: 'docs/as-library.md',
  };
}

/**
 * @param {AppInput} appInput
 * @returns {Promise<DiagnosticCheck[]>}
 */
async function checkInternetPermission(appInput) {
  if (appInput.kind === 'apk') {
    if (appInput.apkHasInternetPermission === true) {
      return [
        {
          id: 'manifest-internet',
          title: 'INTERNET permission',
          status: 'pass',
          message: 'APK declares android.permission.INTERNET (required for the Espresso HTTP server).',
        },
      ];
    }
    if (appInput.apkHasInternetPermission === false) {
      return [
        {
          id: 'manifest-internet',
          title: 'INTERNET permission',
          status: 'fail',
          message:
            'APK does not declare android.permission.INTERNET. Add <uses-permission android:name="android.permission.INTERNET" /> to the AUT manifest and reinstall.',
          docRef: 'README.md#troubleshooting',
        },
      ];
    }
    return [
      {
        id: 'manifest-internet',
        title: 'INTERNET permission',
        status: 'info',
        message:
          'Could not verify INTERNET on the APK (install Android build-tools for aapt/aapt2, or use a Gradle project path).',
      },
    ];
  }

  const corpus = appInput.gradleCorpus ?? '';
  const manifests = appInput.manifestPaths ?? [];
  const hasInternet =
    /android\.permission\.INTERNET/.test(corpus) ||
    manifests.some((m) => /android\.permission\.INTERNET/.test(m));

  if (hasInternet) {
    return [
      {
        id: 'manifest-internet',
        title: 'INTERNET permission',
        status: 'pass',
        message:
          'AUT manifest includes android.permission.INTERNET (required — the server socket runs in the app process).',
      },
    ];
  }
  return [
    {
      id: 'manifest-internet',
      title: 'INTERNET permission',
      status: 'fail',
      message:
        'No android.permission.INTERNET found under app/src/main/AndroidManifest.xml (or module manifests). Add it to the AUT manifest and bump versionCode before reinstalling.',
      docRef: 'README.md#troubleshooting',
    },
  ];
}

/**
 * @param {AppInput} appInput
 * @param {EspressoServerDefaults} serverDefaults
 * @returns {DiagnosticCheck[]}
 */
function checkObfuscation(appInput, serverDefaults) {
  /** @type {DiagnosticCheck[]} */
  const results = [];

  if (appInput.minifyEnabled === true) {
    results.push({
      id: 'obfuscation-minify',
      title: 'Code shrinking (R8/ProGuard)',
      status: 'fail',
      message:
        'Gradle enables minifyEnabled/shrinking for at least one build type. Prebuilt Espresso server tests need unobfuscated AUT bytecode (or explicit Keeper rules). Use a debug/non-minified variant for the embedded server module.',
      docRef: 'docs/as-library.md',
    });
  }

  if (appInput.proguardLikely) {
    results.push({
      id: 'obfuscation-apk',
      title: 'Code shrinking (R8/ProGuard)',
      status: 'fail',
      message:
        'APK appears obfuscated/minified. Dependency alignment cannot be verified from the APK; use a debug APK or Gradle project. See slackhq/keeper for shrinker rules when precompiling against release builds.',
      docRef: 'docs/as-library.md',
    });
    results.push({
      id: 'server-versions-reference',
      title: 'Espresso server default versions',
      status: 'info',
      message: `Driver ${serverDefaults.driverVersion} bundles: ${formatServerVersionList(serverDefaults.versions)}`,
    });
    return results;
  }

  if (results.length === 0) {
    results.push({
      id: 'obfuscation-minify',
      title: 'Code shrinking (R8/ProGuard)',
      status: 'pass',
      message: 'No minify/obfuscation indicators detected in scanned inputs.',
    });
  }
  return results;
}

/**
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck[]}
 */
function checkAndroidX(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const usesAndroidX = /android\.useAndroidX\s*=\s*true/i.test(appInput.gradleCorpus ?? '');
  if (usesAndroidX) {
    return [
      {
        id: 'androidx-migration',
        title: 'AndroidX',
        status: 'pass',
        message: 'android.useAndroidX=true is set in gradle.properties.',
      },
    ];
  }
  if (/com\.android\.support\./.test(appInput.gradleCorpus ?? '')) {
    return [
      {
        id: 'androidx-migration',
        title: 'AndroidX',
        status: 'fail',
        message:
          'Legacy Android Support Library references detected without android.useAndroidX=true. Migrate the AUT to AndroidX before embedding the Espresso server.',
      },
    ];
  }
  return [
    {
      id: 'androidx-migration',
      title: 'AndroidX',
      status: 'warn',
      message:
        'android.useAndroidX=true not found in scanned gradle.properties files. Confirm the AUT uses AndroidX artifacts.',
    },
  ];
}

/**
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck[]}
 */
function checkInitializationProvider(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const corpus = `${appInput.gradleCorpus ?? ''}\n${(appInput.manifestPaths ?? []).join('\n')}`;
  if (!/InitializationProvider/.test(corpus)) {
    return [
      {
        id: 'startup-provider',
        title: 'App Startup provider',
        status: 'pass',
        message: 'androidx.startup.InitializationProvider not found in scanned manifests.',
      },
    ];
  }
  if (/tools:node\s*=\s*["']remove["']/.test(corpus) && /InitializationProvider/.test(corpus)) {
    return [
      {
        id: 'startup-provider',
        title: 'App Startup provider',
        status: 'pass',
        message: 'InitializationProvider is explicitly removed for instrumentation (recommended for Espresso fixtures).',
      },
    ];
  }
  return [
    {
      id: 'startup-provider',
      title: 'App Startup provider',
      status: 'warn',
      message:
        'InitializationProvider is present without tools:node="remove". Under instrumentation this can cause Resources$NotFoundException for androidx_startup. Remove the provider or add startup-runtime to the AUT.',
      docRef: 'docs/compose-troubleshooting.md',
    },
  ];
}

/**
 * @param {AppInput} appInput
 * @param {EspressoServerDefaults} serverDefaults
 * @param {import('./dependency-versions.mjs').ComparisonReport} dependencyReport
 * @returns {DiagnosticCheck[]}
 */
function checkCompileSdk(appInput, serverDefaults, dependencyReport) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const appCompileSdk = parseCompileSdkFromCorpus(appInput.gradleCorpus ?? '');
  const serverCompileSdk = serverDefaults.compileSdk ? parseInt(serverDefaults.compileSdk, 10) : null;
  const composeVersion = dependencyReport.modules.find((m) => m.id === 'compose')?.appVersion;
  const composeMinor = composeVersion ? semver.coerce(composeVersion)?.minor : null;
  const minComposeCompileSdk = composeMinor != null && composeMinor >= 11 ? 35 : null;

  /** @type {DiagnosticCheck[]} */
  const checks = [];

  if (appCompileSdk && serverCompileSdk && appCompileSdk < serverCompileSdk) {
    checks.push({
      id: 'compile-sdk',
      title: 'compileSdk',
      status: 'warn',
      message: `AUT compileSdk ${appCompileSdk} is lower than the driver server default (${serverCompileSdk}). When using dynamic server builds, set toolsVersions.compileSdk in espressoBuildConfig.`,
      espressoBuildConfig: {toolsVersions: {compileSdk: String(serverCompileSdk)}},
    });
  } else if (appCompileSdk) {
    checks.push({
      id: 'compile-sdk',
      title: 'compileSdk',
      status: 'pass',
      message: `AUT compileSdk ${appCompileSdk} meets or exceeds the driver default (${serverCompileSdk ?? 'n/a'}).`,
    });
  }

  if (minComposeCompileSdk && appCompileSdk && appCompileSdk < minComposeCompileSdk) {
    checks.push({
      id: 'compose-compile-sdk',
      title: 'Compose compileSdk requirement',
      status: 'fail',
      message: `Compose ${composeVersion} typically requires compileSdk ${minComposeCompileSdk}+ on the Espresso server build. Raise the AUT compileSdk or pass toolsVersions.compileSdk in espressoBuildConfig.`,
      espressoBuildConfig: {toolsVersions: {compileSdk: String(minComposeCompileSdk)}},
      docRef: 'docs/compose-troubleshooting.md',
    });
  }

  return checks;
}

/**
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck[]}
 */
function checkLifecycleExtensionsPin(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const corpus = appInput.gradleCorpus ?? '';
  if (!/lifecycle-extensions/.test(corpus)) {
    return [];
  }
  return [
    {
      id: 'lifecycle-extensions',
      title: 'Legacy lifecycle-extensions',
      status: 'warn',
      message:
        'lifecycle-extensions detected. Avoid pinning it in additionalAndroidTestDependencies — it often causes ProcessLifecycleOwner / lifecycle NoSuchMethodError with modern Compose.',
      docRef: 'docs/compose-troubleshooting.md',
    },
  ];
}

/**
 * @param {AppInput} appInput
 * @returns {DiagnosticCheck[]}
 */
function checkEspressoServerEmbedding(appInput) {
  if (appInput.kind === 'apk') {
    return [
      {
        id: 'espresso-server-embed',
        title: 'Espresso server library',
        status: 'info',
        message:
          'Precompile embeds io.appium.espressoserver:library in an androidTest module — verify manually per docs/as-library.md.',
        docRef: 'docs/as-library.md',
      },
    ];
  }
  const corpus = appInput.gradleCorpus ?? '';
  const embedded =
    /io\.appium\.espressoserver/.test(corpus) || /espressoserver:library/.test(corpus);
  if (embedded) {
    return [
      {
        id: 'espresso-server-embed',
        title: 'Espresso server library',
        status: 'pass',
        message: 'io.appium.espressoserver dependency reference found in Gradle files.',
        docRef: 'docs/as-library.md',
      },
    ];
  }
  return [
    {
      id: 'espresso-server-embed',
      title: 'Espresso server library',
      status: 'info',
      message:
        'No io.appium.espressoserver:library reference yet. Add an androidTest (or com.android.test) module per docs/as-library.md before using skipServerInstallation.',
      docRef: 'docs/as-library.md',
    },
  ];
}

/** @type {Promise<import('appium-adb').ADB> | undefined} */
let manifestAdbPromise;

/**
 * ADB instance for local APK manifest reads (aapt2 via ANDROID_SDK_ROOT).
 * @returns {Promise<import('appium-adb').ADB>}
 */
async function getManifestAdb() {
  if (!manifestAdbPromise) {
    manifestAdbPromise = ADB.createADB({suppressKillServer: true});
  }
  return manifestAdbPromise;
}

/**
 * @param {string} apkPath
 * @returns {Promise<boolean | null>}
 */
async function detectApkInternetPermission(apkPath) {
  try {
    const adb = await getManifestAdb();
    return await adb.hasInternetPermissionFromManifest(apkPath);
  } catch {
    return null;
  }
}

/**
 * @param {string} root
 * @returns {Promise<string[]>}
 */
async function findManifestFiles(root) {
  /** @type {string[]} */
  const manifests = [];
  const candidates = ['app/src/main/AndroidManifest.xml', 'src/main/AndroidManifest.xml'];
  for (const rel of candidates) {
    try {
      manifests.push(await fs.readFile(path.join(root, rel), 'utf8'));
    } catch {
      // continue
    }
  }
  return manifests;
}

/**
 * @param {string} text
 */
function parseGradleProperties(text) {
  /** @type {Record<string, string>} */
  const props = {};
  for (const line of text.split(/\r?\n/)) {
    const match = line.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.+)$/);
    if (match) {
      props[match[1]] = match[2].trim();
    }
  }
  return props;
}

/**
 * @param {string} corpus
 */
function parseCompileSdkFromCorpus(corpus) {
  const patterns = [
    /compileSdk\s*=\s*(\d+)/,
    /compileSdkVersion\s*=\s*(\d+)/,
    /compileSdk\s+(\d+)/,
  ];
  let max = 0;
  for (const pattern of patterns) {
    let match;
    const re = new RegExp(pattern.source, 'g');
    while ((match = re.exec(corpus)) !== null) {
      max = Math.max(max, parseInt(match[1], 10));
    }
  }
  return max > 0 ? max : null;
}

/**
 * @param {Record<string, string>} versions
 */
function formatServerVersionList(versions) {
  return Object.entries(versions)
    .map(([k, v]) => `${k}=${v}`)
    .join(', ');
}

/**
 * @param {DiagnosticCheck[]} checks
 * @param {import('./dependency-versions.mjs').ComparisonReport} dependencyReport
 */
function mergeAllEspressoBuildConfig(checks, dependencyReport) {
  /** @type {Record<string, unknown>} */
  const config = {};
  /** @type {Record<string, string>} */
  const toolsVersions = {};

  for (const check of checks) {
    const fragment = check.espressoBuildConfig;
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

  for (const mod of dependencyReport.modules) {
    const fragment = mod.recommendation.espressoBuildConfig;
    if (fragment?.composeSupport === false) {
      config.composeSupport = false;
    }
    if (fragment?.toolsVersions) {
      Object.assign(toolsVersions, fragment.toolsVersions);
    }
  }

  if (Object.keys(toolsVersions).length) {
    config.toolsVersions = toolsVersions;
  }
  return config;
}
