/**
 * @param {import('./types.mjs').ModuleComparison[]} modules
 */
export function mergeEspressoBuildConfigSuggestions(modules) {
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
 * @param {import('./types.mjs').ComparisonReport} report
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
