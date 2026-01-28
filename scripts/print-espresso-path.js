const path = require('node:path');

function printEspressoServerPath () {
  const dstPath = path.resolve(__dirname, '..', 'espresso-server');
  console.log(dstPath); // eslint-disable-line no-console
}

printEspressoServerPath();
