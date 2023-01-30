const path = require('path');
const { asyncify } = require('asyncbox');
const { logger } = require('@appium/support');
const { ServerBuilder } = require('../build/lib/server-builder.js');

const LOG = new logger.getLogger('EspressoBuild');

const ROOT_DIR = path.resolve(__dirname, '..');
const ESPRESSO_SERVER_ROOT = path.join(ROOT_DIR, 'espresso-server');

async function buildEspressoServer () {
  const opts = {
    serverPath: ESPRESSO_SERVER_ROOT,
    showGradleLog: true,
  };

  if (process.env.TEST_APP_PACKAGE) {
    opts.testAppPackage = process.env.TEST_APP_PACKAGE;
  }

  const builder = new ServerBuilder(LOG, opts);
  await builder.build();
}

if (require.main === module) {
  asyncify(buildEspressoServer);
}

module.exports = buildEspressoServer;
