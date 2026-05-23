import {buildComparisonReport} from '../dependency-versions/index.mjs';
import {mergeAllEspressoBuildConfig} from './espresso-build-config.mjs';
import {buildDiagnosisSummary} from './summary.mjs';
import {
  checkAndroidX,
  checkCompileSdk,
  checkInitializationProvider,
  checkInternetPermission,
  checkLifecycleExtensionsPin,
  checkObfuscation,
  checkPrecompileInputKind,
  mapDependencyChecks,
} from './checks/index.mjs';

/**
 * @param {import('./types.mjs').AppInput} appInput
 * @param {import('./types.mjs').EspressoServerDefaults} serverDefaults
 * @returns {Promise<import('./types.mjs').DiagnosisReport>}
 */
export async function runDiagnosis(appInput, serverDefaults) {
  const dependencyReport = buildComparisonReport(serverDefaults.versions, appInput.versions, {
    proguardLikely: appInput.proguardLikely,
    minifyEnabled: appInput.minifyEnabled,
    detectionSource: appInput.kind === 'apk' ? 'apk' : 'project',
  });

  /** @type {import('./types.mjs').DiagnosticCheck[]} */
  const checks = [];

  checks.push(checkPrecompileInputKind(appInput));
  checks.push(...(await checkInternetPermission(appInput)));
  checks.push(...checkObfuscation(appInput, serverDefaults));
  checks.push(...checkAndroidX(appInput));
  checks.push(...checkInitializationProvider(appInput));
  checks.push(...checkCompileSdk(appInput, serverDefaults, dependencyReport));
  checks.push(...checkLifecycleExtensionsPin(appInput));
  checks.push(...mapDependencyChecks(dependencyReport, appInput));

  const failCount = checks.filter((c) => c.status === 'fail').length;
  const warnCount = checks.filter((c) => c.status === 'warn').length;
  const ready = failCount === 0;

  return {
    ready,
    failCount,
    warnCount,
    checks,
    dependencyReport,
    serverDefaults,
    input: appInput,
    mergedEspressoBuildConfig: mergeAllEspressoBuildConfig(checks, dependencyReport),
    summary: buildDiagnosisSummary(appInput, ready, failCount, warnCount),
  };
}
