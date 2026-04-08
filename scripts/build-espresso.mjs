import _ from 'lodash';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {Command} from 'commander';
import {logger, fs} from 'appium/support.js';
import {ServerBuilder} from '../build/lib/server-builder.js';

const LOG = logger.getLogger('EspressoBuild');

const ROOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');
const ESPRESSO_SERVER_BUILD = path.join(ESPRESSO_SERVER_ROOT, 'app', 'build');

/**
 * @param {BuildOptions} options
 */
async function buildEspressoServer(options) {
  LOG.info(`Building espresso server in '${ESPRESSO_SERVER_BUILD}'`);

  const opts = {
    serverPath: ESPRESSO_SERVER_ROOT,
    showGradleLog: options.showGradleLog,
  };

  if (options.testAppPackage) {
    opts.testAppPackage = options.testAppPackage;
  }

  if (options.buildConfig) {
    if (!(await fs.exists(options.buildConfig))) {
      throw new Error(
        `Cannot find build config at '${options.buildConfig}'. Please provide a valid absolute path to it.`
      );
    }
    try {
      const buildConfigurationStr = await fs.readFile(options.buildConfig, 'utf8');
      opts.buildConfiguration = JSON.parse(buildConfigurationStr);
      LOG.info(`The espresso build config is ${JSON.stringify(opts.buildConfiguration)}`);
    } catch (e) {
      throw new Error(
        `Failed to parse the build config at '${options.buildConfig}'. ` +
          `Please make sure it is a valid JSON file.`, {cause: e}
      );
    }
  }

  const builder = new ServerBuilder(LOG, opts);
  try {
    await builder.build();
  } catch (e) {
    let errorMessage = `Failed to build the espresso server`;
    if (!isGradleLogEnabled(options)) {
      errorMessage += `. Set SHOW_GRADLE_LOG environment variable to true to check the gradle log.`;
    }
    throw new Error(errorMessage, {cause: e});
  }

  const dstPath = path.resolve(
    ESPRESSO_SERVER_BUILD,
    'outputs',
    'apk',
    'androidTest',
    'debug',
    'app-debug-androidTest.apk',
  );
  if (await fs.exists(dstPath)) {
    LOG.info(`Full path to the server APK: ${dstPath}`);
  } else {
    LOG.info(`Full path to the server build folder: ${ESPRESSO_SERVER_BUILD}`);
  }
}

/**
 * @param {BuildOptions} options
 * @returns {boolean}
 */
function isGradleLogEnabled(options) {
  return Boolean(options.showGradleLog ||
    !_.isEmpty(process.env.SHOW_GRADLE_LOG) && ['1', 'true'].includes(_.toLower(process.env.SHOW_GRADLE_LOG))
  );
}

async function main() {
  const program = new Command();
  program
    .name('appium driver run espresso build-espresso')
    .description('Build the Espresso server APK')
    .option('--show-gradle-log', 'Show Gradle logs during build')
    .option('--test-app-package <package>', 'App package to target for test server build')
    .option('--build-config <path>', 'Absolute path to a JSON build configuration file')
    .action(async (options) => {
      await buildEspressoServer({
        showGradleLog: isGradleLogEnabled(options),
        testAppPackage: options.testAppPackage ?? process.env.TEST_APP_PACKAGE,
        buildConfig: options.buildConfig ?? process.env.ESPRESSO_BUILD_CONFIG,
      });
    });

  await program.parseAsync(process.argv);
}

await main();

/**
 * @typedef {Object} BuildOptions
 * @property {boolean | undefined} showGradleLog
 * @property {string | undefined} testAppPackage
 * @property {string | undefined} buildConfig
 */
