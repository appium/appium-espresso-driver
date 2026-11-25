import path from 'node:path';
import {fileURLToPath} from 'node:url';

import {parseCliArgs} from './lib/cli.mjs';

const USAGE = `Usage: appium driver run espresso print-espresso-path

Print the Espresso server path

Options:
  -h, --help  Show this help`;

/**
 * @returns {void}
 */
function printEspressoServerPath() {
  const dstPath = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', 'espresso-server');
  console.log(dstPath); // eslint-disable-line no-console
}

parseCliArgs(USAGE);
printEspressoServerPath();
