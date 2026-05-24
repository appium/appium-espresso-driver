import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {access} from 'node:fs/promises';
import {constants as fsConstants} from 'node:fs';
import {Command} from 'commander';
import {logger} from 'appium/support.js';
import {
  collectAppInputFromApk,
  collectAppInputFromProject,
  formatDiagnosisReport,
  loadEspressoServerDefaults,
  runDiagnosis,
} from './lib/diagnose/index.mjs';

const LOG = logger.getLogger('EspressoDiagnose');

const ROOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');

/**
 * @param {string} targetPath
 */
async function pathExists(targetPath) {
  try {
    await access(targetPath, fsConstants.F_OK);
    return true;
  } catch {
    return false;
  }
}

/**
 * @param {string} inputPath
 */
async function resolveAppInput(inputPath) {
  const resolved = path.resolve(inputPath);
  if (!(await pathExists(resolved))) {
    throw new Error(`Path does not exist: ${resolved}`);
  }
  if (resolved.endsWith('.apk')) {
    return collectAppInputFromApk(resolved);
  }
  if (resolved.endsWith('.aab')) {
    throw new Error('Only .apk files are supported for binary analysis. Use the Gradle project root for .aab.');
  }
  return collectAppInputFromProject(resolved);
}

async function main() {
  const program = new Command();
  program
    .name('appium driver run espresso diagnose-app')
    .description(
      'Diagnose whether an Android app is ready to embed a precompiled Espresso server',
    )
    .requiredOption(
      '--app <path>',
      'Gradle project root of the AUT, or path to a built .apk (debug APK recommended)',
    )
    .action(async (options) => {
      const [appInput, serverDefaults] = await Promise.all([
        resolveAppInput(options.app),
        loadEspressoServerDefaults(ESPRESSO_SERVER_ROOT, ROOT_DIR),
      ]);
      const report = await runDiagnosis(appInput, serverDefaults);

      LOG.info(`App: ${appInput.kind} at ${appInput.path}`);
      LOG.info(`Driver: appium-espresso-driver@${serverDefaults.driverVersion}`);
      if (appInput.sources?.length) {
        LOG.info(
          `Scanned: ${appInput.sources.slice(0, 8).join(', ')}${appInput.sources.length > 8 ? '…' : ''}`,
        );
      }
      LOG.info('');
      LOG.info(formatDiagnosisReport(report));
      if (!report.ready) {
        process.exitCode = 1;
      }
    });

  await program.parseAsync(process.argv);
}

await main();
