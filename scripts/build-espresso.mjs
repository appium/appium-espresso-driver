import path from 'node:path';
import {fileURLToPath, pathToFileURL} from 'node:url';

import {logger, fs} from 'appium/support.js';

import {parseCliArgs} from './lib/cli.mjs';

const USAGE = `Usage: appium driver run espresso build-espresso [options]

Build the Espresso server APK

Options:
  --show-gradle-log            Show Gradle logs during build
  --test-app-package <package> App package to target for test server build
  --build-config <path>        Absolute path to a JSON build configuration file
  -h, --help                   Show this help`;

const LOG = logger.getLogger('EspressoBuild');

const ROOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const SERVER_BUILDER_MODULE = pathToFileURL(path.join(ROOT_DIR, 'build/lib/commands/server/index.js')).href;
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');
const ESPRESSO_SERVER_BUILD = path.join(ESPRESSO_SERVER_ROOT, 'app', 'build');

/**
 * @param {BuildOptions} options
 */
async function buildEspressoServer(options) {
  LOG.info(`Building espresso server in '${ESPRESSO_SERVER_BUILD}'`);

  /** @type {import('../lib/commands/server/builder.js').ServerBuilderOptions} */
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
        `Cannot find build config at '${options.buildConfig}'. Please provide a valid absolute path to it.`,
      );
    }
    try {
      const buildConfigurationStr = await fs.readFile(options.buildConfig, 'utf8');
      opts.buildConfiguration = JSON.parse(buildConfigurationStr);
      LOG.info(`The espresso build config is ${JSON.stringify(opts.buildConfiguration)}`);
    } catch (e) {
      throw new Error(
        `Failed to parse the build config at '${options.buildConfig}'. Please make sure it is a valid JSON file.`,
        {cause: e},
      );
    }
  }

  const {ServerBuilder} = await import(SERVER_BUILDER_MODULE);
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
 * @param {Pick<BuildOptions, 'showGradleLog'>} options
 * @returns {boolean}
 */
function isGradleLogEnabled(options) {
  const showGradleLogEnv = process.env.SHOW_GRADLE_LOG?.toLowerCase();
  return Boolean(options.showGradleLog || (showGradleLogEnv && ['1', 'true'].includes(showGradleLogEnv)));
}

async function main() {
  const {values} = parseCliArgs(USAGE, {
    'show-gradle-log': {type: 'boolean'},
    'test-app-package': {type: 'string'},
    'build-config': {type: 'string'},
  });
  await buildEspressoServer({
    showGradleLog: isGradleLogEnabled({showGradleLog: values['show-gradle-log']}),
    testAppPackage: values['test-app-package'] ?? process.env.TEST_APP_PACKAGE,
    buildConfig: values['build-config'] ?? process.env.ESPRESSO_BUILD_CONFIG,
  });
}

await main();

/**
 * @typedef {Object} BuildOptions
 * @property {boolean | undefined} showGradleLog
 * @property {string | undefined} testAppPackage
 * @property {string | undefined} buildConfig
 */
