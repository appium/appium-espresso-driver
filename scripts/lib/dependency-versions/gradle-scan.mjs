import path from 'node:path';

import {fs} from 'appium/support.js';

import {findFiles} from '../find-files.mjs';
import {TRACKED_MODULES, createEmptyVersionSets} from './tracked-modules.mjs';
import {
  normalizeVersion,
  parseCatalogAccessors,
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
 * @param {string} text
 * @param {string} accessor
 * @returns {boolean}
 */
function isReferenced(text, accessor) {
  // the exact accessor, optionally followed by a provider method (`libs.ui` is not `libs.ui.core`)
  const methods = 'get|getOrNull|getOrElse|asProvider|orNull|map|flatMap|zip|isPresent';
  return new RegExp(`(?<![\\w.])${accessor.replace(/\./g, '\\.')}(?!\\w)(?!\\.(?!(?:${methods})\\b)\\w)`).test(text);
}

/**
 * The directory of the Gradle build a version catalog belongs to.
 * @param {string} catalogPath
 * @returns {string}
 */
function catalogBuildRoot(catalogPath) {
  const dir = path.dirname(catalogPath);
  return path.basename(dir) === 'gradle' ? path.dirname(dir) : dir;
}

/**
 * @param {string} projectRoot
 * @returns {Promise<{versions: Record<string, string[]>, minifyEnabled: boolean | null, sources: string[]}>}
 */
export async function collectAppVersionsFromProject(projectRoot) {
  const gradleFiles = await findGradleFiles(projectRoot);
  const found = createEmptyVersionSets();
  let minifyEnabled = null;
  /** @type {{root: string, text: string}[]} */
  const catalogs = [];
  /** @type {{dir: string, text: string}[]} */
  const scripts = [];

  for (const filePath of gradleFiles) {
    const text = await fs.readFile(filePath, 'utf8');
    if (filePath.endsWith('libs.versions.toml')) {
      catalogs.push({root: catalogBuildRoot(filePath), text});
    } else {
      scripts.push({dir: path.dirname(filePath), text: stripComments(text)});
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
  }

  // A catalog version counts only if a script of the build owning the catalog consumes it
  const roots = catalogs.map((c) => c.root);
  const ownerOf = (/** @type {string} */ dir) =>
    roots
      .filter((root) => dir === root || dir.startsWith(root + path.sep))
      .reduce((best, root) => (root.length > best.length ? root : best), '');
  for (const {root, text: catalogText} of catalogs) {
    const ownScripts = scripts
      .filter((s) => ownerOf(s.dir) === root)
      .map((s) => s.text)
      .join('\n');
    const catalogVersions = parseVersionsToml(catalogText);
    const accessors = parseCatalogAccessors(catalogText);
    for (const mod of TRACKED_MODULES) {
      const normalized = normalizeVersion(catalogVersions[mod.catalogKey]);
      if (normalized && (accessors[mod.catalogKey] ?? []).some((acc) => isReferenced(ownScripts, acc))) {
        found[mod.id].add(normalized);
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
