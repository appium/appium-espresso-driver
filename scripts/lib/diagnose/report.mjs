import {formatReport as formatDependencyReport} from '../dependency-versions/index.mjs';

/**
 * @param {import('./types.mjs').DiagnosisReport} report
 */
export function formatDiagnosisReport(report) {
  const lines = [];
  lines.push('Espresso precompile readiness diagnosis');
  lines.push('========================================');
  lines.push('');
  const verdict = report.ready ? 'READY' : 'NOT READY';
  lines.push(`Verdict: ${verdict} — ${report.summary}`);
  lines.push('');

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
    lines.push('Next: build and install the AUT with an Espresso server version compatible with this driver.');
  }

  return lines.join('\n');
}

/**
 * @param {import('./types.mjs').CheckStatus} s
 */
function statusIcon(s) {
  if (s === 'pass') {return '[PASS]';}
  if (s === 'warn') {return '[WARN]';}
  if (s === 'fail') {return '[FAIL]';}
  if (s === 'skip') {return '[SKIP]';}
  return '[INFO]';
}
