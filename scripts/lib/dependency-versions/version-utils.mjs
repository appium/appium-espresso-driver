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
  const braceDelta = (/** @type {string} */ line) =>
    (line.match(/\{/g)?.length ?? 0) - (line.match(/\}/g)?.length ?? 0);
  /** @type {Record<string, string[]>} */
  const accessors = {};
  /** @typedef {{section: string, alias: string, body: string}} CatalogEntry */
  /** @type {CatalogEntry | null} */
  let current = null;
  let section = '';
  let depth = 0;

  const add = (/** @type {CatalogEntry} */ entry) => {
    if (!(entry.section in prefixes)) {
      return;
    }
    const prefix = prefixes[/** @type {keyof typeof prefixes} */ (entry.section)];
    const key =
      entry.section === 'versions' ? entry.alias : entry.body.match(/version\.ref\s*=\s*["']([^"']+)["']/)?.[1];
    if (key) {
      (accessors[key] ??= []).push(`${prefix}.${dotted(entry.alias)}`);
    }
  };

  for (const line of text.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) {
      continue;
    }
    if (depth > 0 && current) {
      // continuation of a multiline inline table
      current.body += ` ${trimmed}`;
      depth += braceDelta(trimmed);
      if (depth <= 0) {
        add(current);
        current = null;
        depth = 0;
      }
      continue;
    }
    // `[libraries.alias]` tables carry the entry's keys on the following lines
    const header = trimmed.match(/^\[(\w+)(?:\.([\w.-]+))?\]$/);
    if (header) {
      if (current) {
        add(current);
      }
      section = header[1];
      current = header[2] ? {section, alias: header[2], body: ''} : null;
      continue;
    }
    const kv = trimmed.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.*)$/);
    if (!kv) {
      continue;
    }
    if (current) {
      current.body += ` ${trimmed}`;
      continue;
    }
    const entry = {section, alias: kv[1], body: kv[2]};
    depth = braceDelta(kv[2]);
    if (depth > 0) {
      current = entry;
    } else {
      depth = 0;
      add(entry);
    }
  }
  if (current) {
    add(current);
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
