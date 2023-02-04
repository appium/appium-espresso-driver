const path = require('path');

function logEspressoServerPath () {
  const dstPath = path.resolve(__dirname, '..', 'espresso-server');
  console.log(dstPath); // eslint-disable-line no-console
}

logEspressoServerPath();
