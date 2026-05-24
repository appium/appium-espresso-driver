import {util} from 'appium/support.js';

/**
 * @param {import('./types.mjs').AppInput} appInput
 * @param {boolean} ready
 * @param {number} failCount
 * @param {number} warnCount
 */
export function buildDiagnosisSummary(appInput, ready, failCount, warnCount) {
  if (appInput.kind === 'apk') {
    return failCount > 0
      ? `APK scan found ${util.pluralize('blocking issue', failCount, true)}.`
      : 'APK scan complete — no blocking issues found in the built artifact.';
  }
  if (ready) {
    return warnCount > 0
      ? `Ready for precompile with ${util.pluralize('warning', warnCount, true)} to review.`
      : 'Ready for precompile into the Espresso driver (library / androidTest module).';
  }
  return `Not ready for precompile (${util.pluralize('blocking issue', failCount, true)}, ${util.pluralize('warning', warnCount, true)}).`;
}
