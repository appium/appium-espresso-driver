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
    if (/^\[versions\]\s*(#.*)?$/.test(trimmed)) {
      inVersionsSection = true;
      continue;
    }
    if (trimmed.startsWith('[')) {
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
  const prefixes = {versions: 'libs.versions', libraries: 'libs', plugins: 'libs.plugins', bundles: 'libs.bundles'};
  const dotted = (/** @type {string} */ name) => name.replace(/[-_]/g, '.');
  const delta = (/** @type {string} */ line) =>
    (line.match(/[{[]/g)?.length ?? 0) - (line.match(/[}\]]/g)?.length ?? 0);
  /** @typedef {{section: string, alias: string, body: string}} CatalogEntry */
  /** @type {CatalogEntry[]} */
  const entries = [];
  /** @type {CatalogEntry | null} */
  let current = null;
  let section = '';
  let depth = 0;

  const finish = () => {
    if (current) {
      entries.push(current);
      current = null;
    }
    depth = 0;
  };

  for (const rawLine of text.split(/\r?\n/)) {
    // drop trailing comments (only those without quotes after them, to keep `#` inside strings intact)
    const trimmed = rawLine.replace(/(^|\s)#[^"']*$/, '').trim();
    if (!trimmed) {
      continue;
    }
    if (depth > 0 && current) {
      // continuation of a multiline inline table or array
      current.body += ` ${trimmed}`;
      depth += delta(trimmed);
      if (depth <= 0) {
        finish();
      }
      continue;
    }
    // `[libraries.alias]` tables carry the entry's keys on the following lines
    const header = trimmed.match(/^\[(\w+)(?:\.([\w.-]+))?\]$/);
    if (header) {
      finish();
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
    current = {section, alias: kv[1], body: kv[2]};
    depth = delta(kv[2]);
    if (depth <= 0) {
      finish();
    }
  }
  finish();

  const versionRef = (/** @type {CatalogEntry} */ entry) =>
    entry.body.match(/version\.ref\s*=\s*["']([^"']+)["']/)?.[1];
  /** @type {Record<string, string[]>} */
  const accessors = {};
  /** @type {Record<string, string>} */
  const libraryRefs = {};
  const add = (/** @type {string | undefined} */ key, /** @type {CatalogEntry} */ entry) => {
    if (key) {
      (accessors[key] ??= []).push(
        `${prefixes[/** @type {keyof typeof prefixes} */ (entry.section)]}.${dotted(entry.alias)}`,
      );
    }
  };
  for (const entry of entries) {
    if (entry.section === 'versions') {
      add(entry.alias, entry);
    } else if (entry.section === 'libraries' || entry.section === 'plugins') {
      const ref = versionRef(entry);
      add(ref, entry);
      if (ref && entry.section === 'libraries') {
        libraryRefs[entry.alias] = ref;
      }
    }
  }
  // a bundle exposes the versions of its member libraries
  for (const entry of entries.filter((e) => e.section === 'bundles')) {
    const members = [...entry.body.matchAll(/["']([^"']+)["']/g)].map((m) => m[1]);
    for (const key of new Set(members.map((alias) => libraryRefs[alias]))) {
      add(key, entry);
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
