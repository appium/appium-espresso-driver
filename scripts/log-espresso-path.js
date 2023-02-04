const path = require('path');
const log = require('fancy-log');

function logEspressoServerPath () {
  const dstPath = path.resolve(__dirname, '..', 'espresso-server');
  log.info(`Appium Espresso server exists in '${dstPath}'`);
}

logEspressoServerPath();
