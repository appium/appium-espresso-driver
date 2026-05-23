import {formatServerVersionList} from '../gradle-utils.mjs';

/**
 * @param {import('../types.mjs').AppInput} appInput
 * @param {import('../types.mjs').EspressoServerDefaults} serverDefaults
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function checkObfuscation(appInput, serverDefaults) {
  /** @type {import('../types.mjs').DiagnosticCheck[]} */
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
        'APK appears obfuscated/minified. Dependency alignment cannot be verified from the APK; use an unobfuscated debug APK if possible. See slackhq/keeper for shrinker rules when testing against release builds.',
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
