import path from 'node:path';
import {fileURLToPath} from 'node:url';

import {logger, fs} from 'appium/support.js';
import semver from 'semver';

import {failWithUsage, parseCliArgs} from './lib/cli.mjs';

const USAGE = `Usage: node ./scripts/sync-version.mjs --package-version <version>

Sync package version into Espresso server module source

Options:
  --package-version <version>  Package version to synchronize (required)
  -h, --help                   Show this help`;

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
  'Version.kt',
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
  const {values} = parseCliArgs(USAGE, {'package-version': {type: 'string'}});
  const version = values['package-version'];
  if (version === undefined) {
    failWithUsage(USAGE, `required option '--package-version <version>' not specified`);
  }
  if (!semver.valid(version)) {
    failWithUsage(
      USAGE,
      `Invalid version specified '${version}'. The value must be a valid semver string like '1.2.3'`,
    );
  }
  await syncModuleVersion(version);
}

await main();
