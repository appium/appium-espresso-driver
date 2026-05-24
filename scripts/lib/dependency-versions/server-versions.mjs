import {fs} from 'appium/support.js';
import path from 'node:path';
import {TRACKED_MODULES} from './tracked-modules.mjs';
import {parseVersionsToml} from './version-utils.mjs';

/**
 * @param {string} espressoServerRoot
 * @returns {Promise<Record<string, string>>}
 */
export async function loadEspressoServerVersions(espressoServerRoot) {
  const tomlPath = path.join(espressoServerRoot, 'gradle', 'libs.versions.toml');
  const text = await fs.readFile(tomlPath, 'utf8');
  const versions = parseVersionsToml(text);
  /** @type {Record<string, string>} */
  const result = {};
  for (const mod of TRACKED_MODULES) {
    const version = versions[mod.catalogKey];
    if (version) {
      result[mod.id] = version;
    }
  }
  return result;
}
