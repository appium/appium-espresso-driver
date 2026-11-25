import {parseArgs} from 'node:util';

/**
 * Prints the error and usage to stderr, then exits with a non-zero code.
 * @param {string} usage
 * @param {string} message
 * @returns {never}
 */
export function failWithUsage(usage, message) {
  process.stderr.write(`error: ${message}\n\n${usage}\n`);
  process.exit(1);
}

/**
 * Strictly parses `process.argv`. Prints `usage` and exits on `-h/--help` (code 0)
 * or on invalid input (code 1).
 * @param {string} usage
 * @param {import('node:util').ParseArgsOptionsConfig} [options]
 * @param {boolean} [allowPositionals]
 * @returns {{values: Record<string, any>, positionals: string[]}}
 */
export function parseCliArgs(usage, options = {}, allowPositionals = false) {
  try {
    const {values, positionals} = parseArgs({
      options: {...options, help: {type: 'boolean', short: 'h'}},
      allowPositionals,
    });
    if (values.help) {
      process.stdout.write(`${usage}\n`);
      process.exit(0);
    }
    return {values, positionals};
  } catch (e) {
    if (/** @type {NodeJS.ErrnoException} */ (e).code?.startsWith('ERR_PARSE_ARGS_')) {
      failWithUsage(usage, /** @type {Error} */ (e).message);
    }
    throw e;
  }
}
