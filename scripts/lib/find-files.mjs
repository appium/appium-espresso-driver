import {glob} from 'node:fs/promises';
import path from 'node:path';

const IGNORED_DIRS = new Set(['node_modules', '.git', '.gradle', 'build']);

/**
 * Finds files matching the pattern under `root`, pruning dependency and build directories.
 *
 * @param {string} pattern Glob pattern
 * @param {string} root Directory to search in
 * @returns {Promise<string[]>} Absolute paths
 */
export async function findFiles(pattern, root) {
  const matches = await Array.fromAsync(
    glob(pattern, {
      cwd: root,
      exclude: (entry) => IGNORED_DIRS.has(path.basename(entry)),
    }),
  );
  return matches.map((match) => path.resolve(root, match));
}
