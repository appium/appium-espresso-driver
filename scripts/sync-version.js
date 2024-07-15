const path = require('node:path');
const { logger, fs } = require('appium/support');
const semver = require('semver');

const LOG = logger.getLogger('VersionSync');

const ROOT_DIR = path.resolve(__dirname, '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');
const VERSION_FILE = path.join(
  ESPRESSO_SERVER_ROOT, 'library', 'src', 'main', 'java',
  'io', 'appium', 'espressoserver',
  'lib', 'helpers', 'Version.kt'
);
const VERSION_PATTERN = /VERSION\s*=\s*"([0-9.]+)"/;

function parseArgValue (argName) {
  const argNamePattern = new RegExp(`^--${argName}\\b`);
  for (let i = 1; i < process.argv.length; ++i) {
    const arg = process.argv[i];
    if (argNamePattern.test(arg)) {
      return arg.includes('=') ? arg.split('=')[1] : process.argv[i + 1];
    }
  }
  return null;
}

async function syncModuleVersion () {
  const origContent = await fs.readFile(VERSION_FILE, 'utf8');
  const espressoVersionMatch = VERSION_PATTERN.exec(origContent);
  if (!espressoVersionMatch) {
    throw new Error(`Could not parse Espresso module version from '${VERSION_FILE}'`);
  }
  const packageVersion = parseArgValue('package-version');
  if (!packageVersion) {
    throw new Error('No package version argument (use `--package-version=xxx`)');
  }
  if (!semver.valid(packageVersion)) {
    throw new Error(
      `Invalid version specified '${packageVersion}'. Version should be in the form '1.2.3'`
    );
  }
  const updatedContent = origContent.replace(espressoVersionMatch[1], packageVersion);
  await fs.writeFile(VERSION_FILE, updatedContent, 'utf8');
  LOG.info(`Synchronized module version '${packageVersion}' to '${VERSION_FILE}'`);
}

(async () => await syncModuleVersion())();
