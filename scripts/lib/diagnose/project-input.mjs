import {fs} from 'appium/support.js';
import path from 'node:path';

/**
 * @param {string} root
 * @returns {Promise<string[]>}
 */
export async function findManifestTexts(root) {
  /** @type {string[]} */
  const manifests = [];
  const candidates = ['app/src/main/AndroidManifest.xml', 'src/main/AndroidManifest.xml'];
  for (const rel of candidates) {
    try {
      manifests.push(await fs.readFile(path.join(root, rel), 'utf8'));
    } catch {
      // continue
    }
  }
  return manifests;
}
