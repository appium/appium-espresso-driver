/**
 * @param {string} text
 * @returns {Record<string, string>}
 */
export function parseGradleProperties(text) {
  /** @type {Record<string, string>} */
  const props = {};
  for (const line of text.split(/\r?\n/)) {
    const match = line.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.+)$/);
    if (match) {
      props[match[1]] = match[2].trim();
    }
  }
  return props;
}

/**
 * @param {string} corpus
 * @returns {number | null}
 */
export function parseCompileSdkFromCorpus(corpus) {
  const patterns = [
    /compileSdk\s*=\s*(\d+)/,
    /compileSdkVersion\s*=\s*(\d+)/,
    /compileSdk\s+(\d+)/,
  ];
  let max = 0;
  for (const pattern of patterns) {
    let match;
    const re = new RegExp(pattern.source, 'g');
    while ((match = re.exec(corpus)) !== null) {
      max = Math.max(max, parseInt(match[1], 10));
    }
  }
  return max > 0 ? max : null;
}

/**
 * @param {Record<string, string>} versions
 */
export function formatServerVersionList(versions) {
  return Object.entries(versions)
    .map(([k, v]) => `${k}=${v}`)
    .join(', ');
}
