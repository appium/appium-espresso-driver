import {fs} from 'appium/support';
import path from 'node:path';
import {escapeRegExp} from './predicates';

/**
 * Recursively copy all files except build directories contents
 * @param sourceBaseDir directory to copy files from
 * @param targetBaseDir directory to copy files to
 * @returns Nothing
 */
export async function copyGradleProjectRecursively(
  sourceBaseDir: string,
  targetBaseDir: string,
): Promise<void> {
  // @ts-ignore it is ok to have the async callback
  await fs.walkDir(sourceBaseDir, true, async (itemPath: string, isDirectory: boolean) => {
    const relativePath = path.relative(sourceBaseDir, itemPath);
    const targetPath = path.resolve(targetBaseDir, relativePath);

    const isInGradleBuildDir = `${path.sep}${itemPath}`.includes(`${path.sep}build${path.sep}`);
    if (isInGradleBuildDir) {
      return false;
    }

    if (isDirectory) {
      await fs.mkdirp(targetPath);
    } else {
      await fs.copyFile(itemPath, targetPath);
    }
    return false;
  });
}

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
