const path = require('path');
const { logger, fs } = require('@appium/support');
const { ServerBuilder } = require('../build/lib/server-builder.js');

const LOG = new logger.getLogger('EspressoBuild');

const ROOT_DIR = path.resolve(__dirname, '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');

const ESPRESSO_SERVER_BUILD = path.join(ESPRESSO_SERVER_ROOT, 'app', 'build');

async function buildEspressoServer () {
  console.log(`Deleting the build directory ${ESPRESSO_SERVER_BUILD}`); // eslint-disable-line no-console

  const opts = {
    serverPath: ESPRESSO_SERVER_ROOT,
    showGradleLog: process.env.SHOW_GRADLE_LOG ? process.env.SHOW_GRADLE_LOG : false
  };

  if (process.env.TEST_APP_PACKAGE) {
    opts.testAppPackage = process.env.TEST_APP_PACKAGE;
  }

  if (process.env.ESPRESSO_BUILD_CONFIG) {
    const buildConfigurationStr = await fs.readFile(process.env.ESPRESSO_BUILD_CONFIG, 'utf8');
    opts.buildConfiguration = JSON.parse(buildConfigurationStr);
    console.log(`The espresso build config is ${JSON.stringify(opts.buildConfiguration)}`); // eslint-disable-line no-console
  }

  const builder = new ServerBuilder(LOG, opts);
  await builder.build();

  const dstPath = path.resolve(ESPRESSO_SERVER_ROOT, 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug');
  console.log(`The built server apk is ${dstPath}`); // eslint-disable-line no-console
}

(async () => await buildEspressoServer())();
