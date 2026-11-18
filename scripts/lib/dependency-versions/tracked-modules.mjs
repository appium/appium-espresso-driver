/** @type {import('./types.mjs').TrackedModule[]} */
export const TRACKED_MODULES = [
  {
    id: 'compose',
    label: 'Jetpack Compose UI Test',
    toolsVersionKey: 'composeVersion',
    catalogKey: 'composeUiTest',
    gradleProperty: 'appiumComposeVersion',
    patterns: [
      /composeUiTest\s*=\s*["']([^"']+)["']/gi,
      /compose-ui-test[^:]*:([\d.]+)/gi,
      /compose\.ui:ui-test[^:]*:([\d.]+)/gi,
      /["']androidx\.compose\.ui:ui-test(?:-junit4)?["'][^:]*:([\d.]+)/gi,
      /composeBom\s*=\s*["']([^"']+)["']/gi,
      /compose-bom:([\d.]+)/gi,
      /androidx\.compose:compose-bom:([\d.]+)/gi,
    ],
  },
];

/**
 * @returns {Record<string, Set<string>>}
 */
export function createEmptyVersionSets() {
  return Object.fromEntries(TRACKED_MODULES.map((m) => [m.id, new Set()]));
}
