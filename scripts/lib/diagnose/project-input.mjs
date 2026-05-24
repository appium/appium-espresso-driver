import {fs} from 'appium/support.js';

/**
 * @param {string} root Gradle project root
 * @returns {Promise<string[]>} Main-manifest file contents (one entry per module)
 */
export async function findManifestTexts(root) {
  const manifestPaths = await fs.glob('**/src/main/AndroidManifest.xml', {
    cwd: root,
    absolute: true,
    ignore: ['**/node_modules/**', '**/.git/**', '**/build/**', '**/.gradle/**'],
  });
  if (!manifestPaths.length) {
    return [];
  }
  const contents = await Promise.all(
    manifestPaths.map((manifestPath) => fs.readFile(manifestPath, 'utf8').catch(() => '')),
  );
  return contents.filter(Boolean);
}
