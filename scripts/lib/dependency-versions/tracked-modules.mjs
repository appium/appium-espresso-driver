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
    ],
  },
  {
    // Kotlin stays shared with the app together with Compose, so its version matters only for Compose apps
    id: 'kotlin',
    label: 'Kotlin',
    onlyWithCompose: true,
    toolsVersionKey: 'kotlin',
    catalogKey: 'kotlin',
    gradleProperty: 'appiumKotlin',
    patterns: [
      /\bkotlin\s*=\s*["']([^"']+)["']/gi,
      /org\.jetbrains\.kotlin:[\w-]+:([\d.]+)/gi,
      /mv=\{\s*(\d+)\s+(\d+)\s+(\d+)\s*\}/gi,
    ],
  },
];

/**
 * @returns {Record<string, Set<string>>}
 */
export function createEmptyVersionSets() {
  return Object.fromEntries(TRACKED_MODULES.map((m) => [m.id, new Set()]));
}
