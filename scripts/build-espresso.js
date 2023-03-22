const _ = require('lodash');
const path = require('path');
const { logger, fs } = require('@appium/support');
const { ServerBuilder } = require('../build/lib/server-builder.js');

const LOG = new logger.getLogger('EspressoBuild');

const ROOT_DIR = path.resolve(__dirname, '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');

const ESPRESSO_SERVER_BUILD = path.join(ESPRESSO_SERVER_ROOT, 'app', 'build');

async function buildEspressoServer () {

  LOG.info(`Deleting the build directory ${ESPRESSO_SERVER_BUILD}`);

  const opts = {
    serverPath: ESPRESSO_SERVER_ROOT,
    showGradleLog: !_.isEmpty(process.env.SHOW_GRADLE_LOG) && ['1', 'true'].includes(_.toLower(process.env.SHOW_GRADLE_LOG))
  };

  if (process.env.TEST_APP_PACKAGE) {
    opts.testAppPackage = process.env.TEST_APP_PACKAGE;
  }

  if (process.env.ESPRESSO_BUILD_CONFIG) {
    if (!(await fs.exists(process.env.ESPRESSO_BUILD_CONFIG))) {
      throw new Error(`'${process.env.ESPRESSO_BUILD_CONFIG}' did not exist. Please set the path as an absolute path.`);
    }
    try {
      const buildConfigurationStr = await fs.readFile(process.env.ESPRESSO_BUILD_CONFIG, 'utf8');
      opts.buildConfiguration = JSON.parse(buildConfigurationStr);
      LOG.info(`The espresso build config is ${JSON.stringify(opts.buildConfiguration)}`);
    } catch (e) {
      throw new Error(`Failed to parse the ${process.env.ESPRESSO_BUILD_CONFIG}. `
        `Please make sure that the JSON is valid format. Error: ${e}`);
    }
  }

  const builder = new ServerBuilder(LOG, opts);
  try {
    await builder.build();
  } catch (e) {
    throw new Error(`Failed to build the espresso server. `
      `SHOW_GRADLE_LOG=true environment variable helps to check the gradle log. Error: ${e}`);
  }

  const dstPath = path.resolve(ESPRESSO_SERVER_ROOT, 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug', 'app-debug-androidTest.apk');
  if (await fs.exists(dstPath)) {
    LOG.info(`Full path to the server APK: ${dstPath}`);
  } else {
    LOG.info(`Full path to the server build folder: ${ESPRESSO_SERVER_BUILD}`);
  }
}

(async () => await buildEspressoServer())();
