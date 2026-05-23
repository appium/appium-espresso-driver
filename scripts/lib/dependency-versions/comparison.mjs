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
  /** @type {import('./types.mjs').ModuleComparison[]} */
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
 * @param {import('./types.mjs').TrackedModule} mod
 * @param {import('./types.mjs').VersionDiffKind} diff
 * @param {string | null} appVersion
 * @param {string | undefined} serverVersion
 * @param {{proguardLikely: boolean, minifyEnabled: boolean | null, appHasCompose: boolean, detectionSource: 'apk' | 'project'}} ctx
 * @returns {import('./types.mjs').Recommendation}
 */
function buildRecommendation(mod, diff, appVersion, serverVersion, ctx) {
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
    if (mod.id === 'compose' && ctx.detectionSource === 'project' && !ctx.appHasCompose) {
      return {
        level: 'suggestion',
        message:
          'App does not declare Compose test dependencies. Consider `"composeSupport": false` in espressoBuildConfig if the AUT is not Compose-based.',
        espressoBuildConfig: {composeSupport: false},
      };
    }
    if (ctx.detectionSource === 'apk') {
      if (mod.id === 'espresso' || mod.id === 'androidxTest' || mod.id === 'uiautomator') {
        return {
          level: 'info',
          message:
            'Not present in the app APK (expected for a main application — Espresso/AndroidX Test libraries are usually in androidTest).',
        };
      }
      return {
        level: 'info',
        message:
          'Not detected in APK scan. If the app uses this library, pass the Gradle project root or ensure AGP embedded META-INF/*.version files are present.',
      };
    }
    return {level: 'ok', message: 'Not detected in the app; no action needed.'};
  }
  if (diff === 'equal' || diff === 'patch') {
    return {level: 'ok', message: 'Versions are compatible.'};
  }
  if (diff === 'minor') {
    return {
      level: 'suggestion',
      message: `Minor version drift. Align the Espresso server via toolsVersions.${mod.toolsVersionKey ?? mod.gradleProperty}.`,
      espressoBuildConfig: mod.toolsVersionKey
        ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
        : {gradleProperty: {[mod.gradleProperty]: appVersion}},
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
        espressoBuildConfig: mod.toolsVersionKey
          ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
          : {gradleProperty: {[mod.gradleProperty]: appVersion}},
        preferAppUpdate: true,
      };
    }
    return {
      level: 'warning',
      message:
        'Large version gap: the app is ahead of the Espresso server defaults. Set matching versions in espressoBuildConfig.toolsVersions before rebuilding the server.',
      espressoBuildConfig: mod.toolsVersionKey
        ? {toolsVersions: {[mod.toolsVersionKey]: appVersion}}
        : {gradleProperty: {[mod.gradleProperty]: appVersion}},
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
