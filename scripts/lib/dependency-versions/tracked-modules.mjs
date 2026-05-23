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
  {
    id: 'espresso',
    label: 'Espresso',
    toolsVersionKey: 'espressoVersion',
    catalogKey: 'espresso',
    gradleProperty: 'appiumEspressoVersion',
    patterns: [
      /\bespresso\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test\.espresso:espresso-[\w-]+:([\d.]+)/gi,
      /espresso-core:([\d.]+)/gi,
    ],
  },
  {
    id: 'annotation',
    label: 'AndroidX Annotation',
    toolsVersionKey: 'annotationVersion',
    catalogKey: 'annotation',
    gradleProperty: 'appiumAnnotationVersion',
    patterns: [
      /\bannotation\s*=\s*["']([^"']+)["']/gi,
      /androidx\.annotation:annotation:([\d.]+)/gi,
    ],
  },
  {
    id: 'androidxTest',
    label: 'AndroidX Test',
    toolsVersionKey: null,
    catalogKey: 'androidxTest',
    gradleProperty: 'appiumAndroidxTestVersion',
    patterns: [
      /androidxTest\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test:(?:core|runner|rules|ext):[\w-]*:([\d.]+)/gi,
    ],
  },
  {
    id: 'uiautomator',
    label: 'UiAutomator',
    toolsVersionKey: null,
    catalogKey: 'uiautomator',
    gradleProperty: 'appiumUiAutomatorVersion',
    patterns: [
      /\buiautomator\s*=\s*["']([^"']+)["']/gi,
      /androidx\.test\.uiautomator:uiautomator:([\d.]+)/gi,
    ],
  },
  {
    id: 'kotlin',
    label: 'Kotlin',
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
