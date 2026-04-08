import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {Command} from 'commander';

/**
 * @returns {void}
 */
function printEspressoServerPath() {
  const dstPath = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', 'espresso-server');
  console.log(dstPath); // eslint-disable-line no-console
}

async function main() {
  const program = new Command();
  program
    .name('appium driver run espresso print-espresso-path')
    .description('Print the Espresso server path')
    .action(() => {
      printEspressoServerPath();
    });

  await program.parseAsync(process.argv);
}

await main();
