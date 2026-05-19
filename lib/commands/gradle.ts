import {escapeRegExp} from '../utils';

/**
 * Insert Gradle dependency lines after a `// placeholder` marker in a Gradle file.
 *
 * @param originalContent - Full text of the Gradle configuration file.
 * @param dependencyPlaceholder - Placeholder comment label to find (e.g. from build.gradle).
 * @param dependencyLines - Dependency lines to insert (e.g. `implementation "..."`).
 * @returns Updated file content, or the original string if the placeholder is missing.
 */
export function updateDependencyLines(
  originalContent: string,
  dependencyPlaceholder: string,
  dependencyLines: string[],
): string {
  const configurationLines = originalContent.split('\n');
  const searchRe = new RegExp(`^\\s*//\\s*\\b${escapeRegExp(dependencyPlaceholder)}\\b`, 'm');
  const placeholderIndex = configurationLines.findIndex((line) => searchRe.test(line));
  if (placeholderIndex < 0) {
    return originalContent;
  }

  const placeholderLine = configurationLines[placeholderIndex];
  const indentLen = placeholderLine.length - placeholderLine.trimStart().length;
  configurationLines.splice(
    placeholderIndex + 1,
    0,
    ...dependencyLines.map((line) => `${' '.repeat(indentLen)}${line}`),
  );
  return configurationLines.join('\n');
}
