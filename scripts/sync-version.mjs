import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {Command} from 'commander';
import {logger, fs} from 'appium/support.js';
import semver from 'semver';

const LOG = logger.getLogger('VersionSync');

const ROOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');
const VERSION_FILE = path.join(
  ESPRESSO_SERVER_ROOT,
  'library',
  'src',
  'main',
  'java',
  'io',
  'appium',
  'espressoserver',
  'lib',
  'helpers',
  'Version.kt'
);
const VERSION_PATTERN = /VERSION\s*=\s*"([0-9.]+)"/;

/**
 * @param {string} packageVersion
 * @returns {Promise<void>}
 */
async function syncModuleVersion(packageVersion) {
  const origContent = await fs.readFile(VERSION_FILE, 'utf8');
  const espressoVersionMatch = VERSION_PATTERN.exec(origContent);
  if (!espressoVersionMatch) {
    throw new Error(`Could not parse Espresso module version from '${VERSION_FILE}'`);
  }
  const updatedContent = origContent.replace(espressoVersionMatch[1], packageVersion);
  await fs.writeFile(VERSION_FILE, updatedContent, 'utf8');
  LOG.info(`Synchronized module version '${packageVersion}' to '${VERSION_FILE}'`);
}

async function main() {
  const program = new Command();
  program
    .name('node ./scripts/sync-version.mjs')
    .description('Sync package version into Espresso server module source')
    .requiredOption(
      '--package-version <version>',
      'Package version to synchronize',
      (value) => {
        if (!semver.valid(value)) {
          throw new Error(
            `Invalid version specified '${value}'. The value must be a valid semver string like '1.2.3'`
          );
        }
        return value;
      }
    )
    .action(async (options) => {
      await syncModuleVersion(options.packageVersion);
    });

  await program.parseAsync(process.argv);
}

await main();
