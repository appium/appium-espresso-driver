import {fs} from 'appium/support.js';
import path from 'node:path';
import {normalizeVersion} from './version-utils.mjs';

/** @type {ReadonlyArray<{moduleId: string, matches: (base: string) => boolean}>} */
const META_INF_VERSION_MODULE_RULES = [
  {moduleId: 'compose', matches: (base) => base.startsWith('androidx.compose.')},
  {moduleId: 'kotlin', matches: (base) => base.startsWith('org.jetbrains.kotlin_')},
  {moduleId: 'espresso', matches: (base) => base.startsWith('androidx.test.espresso')},
  {
    moduleId: 'annotation',
    matches: (base) =>
      base.startsWith('androidx.annotation_annotation') && !base.includes('experimental'),
  },
  {moduleId: 'uiautomator', matches: (base) => base.startsWith('androidx.test.uiautomator')},
  {
    moduleId: 'androidxTest',
    matches: (base) =>
      base.startsWith('androidx.test.') &&
      !base.startsWith('androidx.test.espresso') &&
      !base.startsWith('androidx.test.uiautomator'),
  },
];

/**
 * @param {string} base Basename without `.version` (e.g. `androidx.compose.ui_ui`)
 * @returns {string | null}
 */
export function mapMetaInfVersionBaseToModule(base) {
  for (const {moduleId, matches} of META_INF_VERSION_MODULE_RULES) {
    if (matches(base)) {
      return moduleId;
    }
  }
  return null;
}

/**
 * @param {string} extractRoot APK extract directory
 * @param {Record<string, Set<string>>} found Module id → version set (mutated)
 */
export async function mergeMetaInfEmbeddedVersions(extractRoot, found) {
  const metaDir = path.join(extractRoot, 'META-INF');
  let versionFiles;
  try {
    versionFiles = await fs.glob('**/*.version', {cwd: metaDir, absolute: true});
  } catch {
    return;
  }
  for (const filePath of versionFiles) {
    const base = path.basename(filePath, '.version');
    let raw;
    try {
      raw = (await fs.readFile(filePath, 'utf8')).trim();
    } catch {
      continue;
    }
    if (!raw || /writeVersionFile|^\s*task\s*:/i.test(raw)) {
      continue;
    }
    const version = normalizeVersion(raw);
    if (!version) {
      continue;
    }
    const moduleId = mapMetaInfVersionBaseToModule(base);
    if (moduleId && found[moduleId]) {
      found[moduleId].add(version);
    }
  }
}
