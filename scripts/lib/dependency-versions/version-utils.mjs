import semver from 'semver';
import {TRACKED_MODULES} from './tracked-modules.mjs';

/**
 * @param {RegExpExecArray} match
 * @returns {string | null}
 */
export function versionFromPatternMatch(match) {
  const raw = match[3] !== undefined ? `${match[1]}.${match[2]}.${match[3]}` : match[1];
  return normalizeVersion(raw);
}

/**
 * @param {string} text
 * @returns {Record<string, string>}
 */
export function parseVersionsToml(text) {
  /** @type {Record<string, string>} */
  const versions = {};
  let inVersionsSection = false;
  for (const line of text.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (trimmed === '[versions]') {
      inVersionsSection = true;
      continue;
    }
    if (trimmed.startsWith('[') && trimmed !== '[versions]') {
      inVersionsSection = false;
      continue;
    }
    if (!inVersionsSection || !trimmed || trimmed.startsWith('#')) {
      continue;
    }
    const match = trimmed.match(/^([A-Za-z0-9_.-]+)\s*=\s*["']([^"']+)["']/);
    if (match) {
      versions[match[1]] = match[2];
    }
  }
  return versions;
}

/**
 * @param {string | undefined} raw
 * @returns {string | null}
 */
export function normalizeVersion(raw) {
  if (!raw) {
    return null;
  }
  return semver.coerce(String(raw).trim(), {includePrerelease: true})?.version ?? null;
}

/**
 * @param {string} a
 * @param {string} b
 */
export function compareVersionsDesc(a, b) {
  const sa = semver.coerce(a);
  const sb = semver.coerce(b);
  if (sa && sb) {
    return semver.rcompare(sa, sb);
  }
  return b.localeCompare(a);
}

/**
 * @param {Record<string, Set<string>>} found
 * @returns {Record<string, string[]>}
 */
export function versionSetsToSortedRecords(found) {
  /** @type {Record<string, string[]>} */
  const versions = {};
  for (const [id, set] of Object.entries(found)) {
    versions[id] = [...set].sort(compareVersionsDesc);
  }
  return versions;
}

/**
 * @param {string} corpus
 * @param {Record<string, Set<string>>} found
 */
export function collectVersionsFromCorpus(corpus, found) {
  for (const mod of TRACKED_MODULES) {
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
