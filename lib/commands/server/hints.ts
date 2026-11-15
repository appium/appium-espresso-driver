const DOCS_URL = 'https://appium.github.io/appium-espresso-driver/latest/troubleshooting/class-conflicts/';
const REPORT = 'Please report this as a bug, and attach the logcat crash buffer and your espressoBuildConfig';
const SHADE_OFF = '"appium:espressoBuildConfig": {"shadeServer": false}';

export interface Hint {
  /** Matches the failure text (instrumentation output, logcat crash buffer, server error) */
  pattern: RegExp;
  /** Anchor of the row that documents the pattern in docs/troubleshooting/class-conflicts.md */
  anchor: string;
  hint: string;
}

/**
 * Only the patterns that have been reproduced are listed here. Every one of them must have
 * a row with the same anchor in the class conflicts troubleshooting page.
 */
export const HINTS: readonly Hint[] = [
  {
    pattern: /IllegalAccessError.*io\.appium\.espressoserver\.shaded/s,
    anchor: 'illegal-access-shaded',
    hint: `The server relocation has failed. ${REPORT}. As a workaround try ${SHADE_OFF}`,
  },
  {
    pattern: /Exception handler was not found via a ServiceLoader/,
    anchor: 'service-loader',
    hint: `A service file of the server was not relocated. ${REPORT}. As a workaround try ${SHADE_OFF}`,
  },
  {
    pattern: /StartupException.*ClassCastException.*Initializer|ViewTreeLifecycleOwner not found/s,
    anchor: 'startup-initializer',
    hint: `The classes shared with the application under test are not consistent. ${REPORT}. As a workaround try ${SHADE_OFF}`,
  },
  {
    pattern: /(NoSuchMethodError|NoClassDefFoundError)[^\n]*androidx\.compose/,
    anchor: 'compose-version',
    hint:
      'The Jetpack Compose version of the application under test differs from the one of the Espresso server. ' +
      'Set "toolsVersions": {"composeVersion": "<version of the app>"} in the espressoBuildConfig capability. ' +
      'If the app has no Compose, set "composeSupport": false instead',
  },
];

/**
 * Finds the hints that apply to the given failure text.
 *
 * @param text - Failure text, e.g. the lines of an instrumentation crash.
 * @returns Hint texts with the link to the docs, without duplicates.
 */
export function findHints(text: string): string[] {
  return [
    ...new Set(
      HINTS.filter(({pattern}) => pattern.test(text)).map(
        ({hint, anchor}) => `Hint: ${hint} (see ${DOCS_URL}#${anchor})`,
      ),
    ),
  ];
}

/**
 * Appends the matching hints to an error message.
 *
 * @param message - The original message.
 * @param text - Failure text to look the hints for in.
 * @returns The message, extended with the hints if there are any.
 */
export function withHints(message: string, text: string): string {
  const hints = findHints(text);
  return hints.length ? `${message}\n${hints.join('\n')}` : message;
}
