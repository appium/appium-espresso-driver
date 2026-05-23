import {fs} from 'appium/support.js';
import path from 'node:path';
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
    for (const mod of TRACKED_MODULES) {
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
