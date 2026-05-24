import {fs} from 'appium/support.js';
import path from 'node:path';
import {TRACKED_MODULES, createEmptyVersionSets} from './tracked-modules.mjs';
import {
  normalizeVersion,
  parseVersionsToml,
  versionFromPatternMatch,
  versionSetsToSortedRecords,
} from './version-utils.mjs';

/** @type {RegExp} Gradle configs that do not affect the main/release app artifact. */
const NON_MAIN_DEP_CONFIG_LINE =
  /^\s*(?:androidTest|test)(?:Implementation|Api|CompileOnly|RuntimeOnly|WearApp)\b/;

/**
 * Keeps dependency declarations that can affect the main app artifact (implementation, api, …).
 * Strips androidTest* and test* configuration blocks/lines.
 * @param {string} text
 * @returns {string}
 */
export function extractMainAppDependencyScopes(text) {
  const lines = text.split('\n');
  /** @type {string[]} */
  const kept = [];
  let skipDepth = 0;

  for (const line of lines) {
    if (skipDepth > 0) {
      skipDepth += parenDelta(line);
      if (skipDepth <= 0) {
        skipDepth = 0;
      }
      continue;
    }
    if (NON_MAIN_DEP_CONFIG_LINE.test(line)) {
      const delta = parenDelta(line);
      if (delta > 0) {
        skipDepth = delta;
      }
      continue;
    }
    kept.push(line);
  }
  return kept.join('\n');
}

/**
 * @param {string} root
 * @param {number} [maxDepth]
 * @returns {Promise<string[]>}
 */
export async function findGradleFiles(root, maxDepth = 6) {
  const matches = await fs.glob(
    '**/{build.gradle,build.gradle.kts,gradle.properties,libs.versions.toml}',
    {
      cwd: root,
      absolute: true,
      ignore: ['**/node_modules/**', '**/.git/**', '**/build/**'],
    },
  );
  return matches.filter((filePath) => {
    const parts = path.relative(root, filePath).split(path.sep).filter(Boolean);
    return parts.length - 1 <= maxDepth;
  });
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
      collectVersionsFromGradleText(text, TRACKED_MODULES, found);
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
        if (mod.testOnly) {
          continue;
        }
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
 * Net change in parenthesis depth outside of string literals.
 * @param {string} line
 * @returns {number}
 */
function parenDelta(line) {
  let depth = 0;
  let inString = false;
  let quote = '';
  for (let i = 0; i < line.length; i++) {
    const c = line[i];
    if (inString) {
      if (c === quote && line[i - 1] !== '\\') {
        inString = false;
      }
      continue;
    }
    if (c === '"' || c === "'") {
      inString = true;
      quote = c;
      continue;
    }
    if (c === '(') {
      depth++;
    } else if (c === ')') {
      depth--;
    }
  }
  return depth;
}

/**
 * @param {string} text
 * @param {import('./types.mjs').TrackedModule[]} modules
 * @param {Record<string, Set<string>>} found
 */
function collectVersionsFromGradleText(text, modules, found) {
  for (const mod of modules) {
    const corpus = mod.testOnly ? extractMainAppDependencyScopes(text) : text;
    if (mod.testOnly && !corpus.trim()) {
      continue;
    }
    for (const pattern of mod.patterns) {
      pattern.lastIndex = 0;
      let match;
      while ((match = pattern.exec(corpus)) !== null) {
        const version = versionFromPatternMatch(match);
        if (version) {
          found[mod.id].add(version);
        }
      }
    }
  }
}
