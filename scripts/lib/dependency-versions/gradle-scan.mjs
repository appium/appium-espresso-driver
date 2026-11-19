import path from 'node:path';

import {fs} from 'appium/support.js';

import {findFiles} from '../find-files.mjs';
import {TRACKED_MODULES, createEmptyVersionSets} from './tracked-modules.mjs';
import {
  normalizeVersion,
  parseVersionsToml,
  versionFromPatternMatch,
  versionSetsToSortedRecords,
} from './version-utils.mjs';

/**
 * @param {string} root
 * @param {number} [maxDepth]
 * @returns {Promise<string[]>}
 */
export async function findGradleFiles(root, maxDepth = 6) {
  const matches = await findFiles('**/{build.gradle,build.gradle.kts,gradle.properties,libs.versions.toml}', root);
  return matches.filter((filePath) => {
    const parts = path.relative(root, filePath).split(path.sep).filter(Boolean);
    return parts.length - 1 <= maxDepth;
  });
}

/**
 * Drops block, `//` and full-line `#` (TOML, properties) comments.
 * @param {string} text
 * @returns {string}
 */
export function stripComments(text) {
  return text
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/(^|[^:])\/\/.*$/gm, '$1')
    .replace(/^\s*#.*$/gm, '');
}

/**
 * Whether Gradle files (build scripts, version catalogs) show that the app uses Jetpack Compose,
 * whatever the way it is declared: the Compose compiler plugin, `buildFeatures`, a BOM or a catalog reference.
 * @param {string} text
 * @returns {boolean}
 */
export function usesCompose(text) {
  // `apply false` only declares the plugin for subprojects (usually in the root script)
  const code = stripComments(text).replace(/^.*\bapply\s+false\b.*$/gm, '');
  return /\bcompose\s*=\s*true\b|kotlin\.plugin\.compose|androidx[.-]compose|\blibs\.[\w.]*compose|compose-?bom/i.test(
    code,
  );
}

/**
 * @param {string} projectRoot
 * @returns {Promise<{versions: Record<string, string[]>, minifyEnabled: boolean | null, sources: string[]}>}
 */
export async function collectAppVersionsFromProject(projectRoot) {
  const gradleFiles = await findGradleFiles(projectRoot);
  const found = createEmptyVersionSets();
  let minifyEnabled = null;

  for (const filePath of gradleFiles) {
    const text = await fs.readFile(filePath, 'utf8');
    const rel = path.relative(projectRoot, filePath);
    if (!rel.endsWith('libs.versions.toml')) {
      collectVersionsFromGradleText(stripComments(text), TRACKED_MODULES, found);
    }
    if (/\bisMinifyEnabled\s*=\s*true\b/.test(text) || /\bminifyEnabled\s+true\b/.test(text)) {
      minifyEnabled = true;
    }
    if (/\bisMinifyEnabled\s*=\s*false\b/.test(text) || /\bminifyEnabled\s+false\b/.test(text)) {
      if (minifyEnabled === null) {
        minifyEnabled = false;
      }
    }
    if (rel.endsWith('libs.versions.toml')) {
      const tomlVersions = parseVersionsToml(text);
      for (const mod of TRACKED_MODULES) {
        const v = tomlVersions[mod.catalogKey];
        if (v) {
          const normalized = normalizeVersion(v);
          if (normalized) {
            found[mod.id].add(normalized);
          }
        }
      }
    }
  }

  return {
    versions: versionSetsToSortedRecords(found),
    minifyEnabled,
    sources: gradleFiles.map((f) => path.relative(projectRoot, f)),
  };
}

/**
 * @param {string} text
 * @param {import('./types.mjs').TrackedModule[]} modules
 * @param {Record<string, Set<string>>} found
 */
function collectVersionsFromGradleText(text, modules, found) {
  for (const mod of modules) {
    for (const pattern of mod.patterns) {
      pattern.lastIndex = 0;
      let match;
      while ((match = pattern.exec(text)) !== null) {
        const version = versionFromPatternMatch(match);
        if (version) {
          found[mod.id].add(version);
        }
      }
    }
  }
}
