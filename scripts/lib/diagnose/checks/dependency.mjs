/**
 * @param {import('../../dependency-versions/types.mjs').ComparisonReport} dependencyReport
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function mapDependencyChecks(dependencyReport, appInput) {
  return dependencyReport.modules
    .filter((mod) => mod.appVersion || mod.recommendation.level !== 'ok')
    .map((mod) => {
      const level = mod.recommendation.level;
      /** @type {import('../types.mjs').CheckStatus} */
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

      return /** @type {import('../types.mjs').DiagnosticCheck} */ ({
        id: `dependency-${mod.id}`,
        title: mod.testOnly ? `${mod.label} in app` : `${mod.label} version`,
        status,
        message: mod.recommendation.message,
        espressoBuildConfig: mod.recommendation.espressoBuildConfig,
      });
    });
}
