/**
 * @param {import('./types.mjs').DiagnosticCheck[]} checks
 * @param {import('../dependency-versions/types.mjs').ComparisonReport} dependencyReport
 */
export function mergeAllEspressoBuildConfig(checks, dependencyReport) {
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
