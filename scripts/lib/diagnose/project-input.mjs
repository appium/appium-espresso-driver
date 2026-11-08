import {fs} from 'appium/support.js';

import {findFiles} from '../find-files.mjs';

/**
 * @param {string} root Gradle project root
 * @returns {Promise<string[]>} Main-manifest file contents (one entry per module)
 */
export async function findManifestTexts(root) {
  const manifestPaths = await findFiles('**/src/main/AndroidManifest.xml', root);
  if (!manifestPaths.length) {
    return [];
  }
  const contents = await Promise.all(
    manifestPaths.map((manifestPath) => fs.readFile(manifestPath, 'utf8').catch(() => '')),
  );
  return contents.filter(Boolean);
}
