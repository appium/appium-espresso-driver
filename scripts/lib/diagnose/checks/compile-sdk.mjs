import semver from 'semver';
import {parseCompileSdkFromCorpus} from '../gradle-utils.mjs';

/**
 * @param {import('../types.mjs').AppInput} appInput
 * @param {import('../types.mjs').EspressoServerDefaults} serverDefaults
 * @param {import('../../dependency-versions/types.mjs').ComparisonReport} dependencyReport
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function checkCompileSdk(appInput, serverDefaults, dependencyReport) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const appCompileSdk = parseCompileSdkFromCorpus(appInput.gradleCorpus ?? '');
  const serverCompileSdk = serverDefaults.compileSdk ? parseInt(serverDefaults.compileSdk, 10) : null;
  const composeVersion = dependencyReport.modules.find((m) => m.id === 'compose')?.appVersion;
  const composeMinor = composeVersion ? semver.coerce(composeVersion)?.minor : null;
  const minComposeCompileSdk = composeMinor != null && composeMinor >= 11 ? 35 : null;

  /** @type {import('../types.mjs').DiagnosticCheck[]} */
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
