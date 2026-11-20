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
 * Maps each `[versions]` key of a version catalog to the accessors (`libs.x.y`) through which
 * the catalog exposes it: the version itself and the libraries/plugins that reference it.
 * @param {string} text
 * @returns {Record<string, string[]>}
 */
export function parseCatalogAccessors(text) {
  const prefixes = {versions: 'libs.versions', libraries: 'libs', plugins: 'libs.plugins'};
  const dotted = (/** @type {string} */ name) => name.replace(/[-_]/g, '.');
  /** @type {Record<string, string[]>} */
  const accessors = {};
  /** @type {string | null} */
  let section = null;
  for (const line of text.split(/\r?\n/)) {
    const trimmed = line.trim();
    const header = trimmed.match(/^\[(\w+)\]$/);
    if (header) {
      section = header[1];
      continue;
    }
    const entry = trimmed.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.*)$/);
    if (!entry || trimmed.startsWith('#') || !section || !(section in prefixes)) {
      continue;
    }
    const prefix = prefixes[/** @type {keyof typeof prefixes} */ (section)];
    const accessor = `${prefix}.${dotted(entry[1])}`;
    const key = section === 'versions' ? entry[1] : entry[2].match(/version\.ref\s*=\s*["']([^"']+)["']/)?.[1];
    if (key) {
      (accessors[key] ??= []).push(accessor);
    }
  }
  return accessors;
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
