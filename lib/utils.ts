import {fs, mkdirp} from 'appium/support';
import _ from 'lodash';
import path from 'node:path';
import _fs from 'node:fs';

// @ts-ignore - __filename is available at runtime in CommonJS
declare const __filename: string;

let PACKAGE_INFO: {manifestPath: string; manifestPayload: Record<string, any>} | null = null;
const MODULE_NAME = 'appium-espresso-driver';

/**
 * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
 *
 * @param {string} activityName
 * @param {string} packageName
 * @returns {string} The qualified activity name
 */
export function qualifyActivityName(activityName: string, packageName: string): string {
  // if either activity or package name is not set
  // or any of these contain wildcards then there is
  // no point in qualifying the activity name
  if ([activityName, packageName].some((name) => !name || _.includes(name, '*'))) {
    return activityName;
  }

  const dotPos = activityName.indexOf('.');
  if (dotPos > 0) {
    return activityName;
  }
  return `${packageName}${dotPos === 0 ? '' : '.'}${activityName}`;
}

/**
 * Recursively copy all files except build directories contents
 * @param {string} sourceBaseDir  directory to copy files from
 * @param {string} targetBaseDir directory to copy files to
 * @returns {Promise<void>}
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
      await mkdirp(targetPath);
    } else {
      await fs.copyFile(itemPath, targetPath);
    }
    return false;
  });
}

export function updateDependencyLines(
  originalContent: string,
  dependencyPlaceholder: string,
  dependencyLines: string[],
): string {
  const configurationLines = originalContent.split('\n');
  const searchRe = new RegExp(`^\\s*//\\s*\\b${_.escapeRegExp(dependencyPlaceholder)}\\b`, 'm');
  const placeholderIndex = configurationLines.findIndex((line) => searchRe.test(line));
  if (placeholderIndex < 0) {
    return originalContent;
  }

  const placeholderLine = configurationLines[placeholderIndex];
  const indentLen = placeholderLine.length - _.trimStart(placeholderLine).length;
  configurationLines.splice(
    placeholderIndex + 1,
    0,
    ...dependencyLines.map((line) => `${' '.repeat(indentLen)}${line}`),
  );
  return configurationLines.join('\n');
}

/**
 * Fetches the module info from package.json
 *
 * @returns {Promise<Record<string, any>>} The full path to module's package.json and its payload
 * @throws {Error} If package.json cannot be found
 */
export async function getPackageInfo(): Promise<{
  manifestPath: string;
  manifestPayload: Record<string, any>;
}> {
  if (PACKAGE_INFO) {
    return PACKAGE_INFO;
  }

  let currentDir = path.dirname(path.resolve(__filename));
  let isAtFsRoot = false;
  while (!isAtFsRoot) {
    const manifestPath = path.join(currentDir, 'package.json');
    try {
      if (await fs.exists(manifestPath)) {
        const manifestPayload = JSON.parse(await fs.readFile(manifestPath, 'utf8'));
        if (manifestPayload.name === MODULE_NAME) {
          PACKAGE_INFO = {
            manifestPath,
            manifestPayload,
          };
          return PACKAGE_INFO;
        }
      }
    } catch {}
    currentDir = path.dirname(currentDir);
    isAtFsRoot = currentDir.length <= path.dirname(currentDir).length;
  }
  throw new Error(`Cannot find the root folder of the ${MODULE_NAME} Node.js module`);
}

/**
 * Fetches the module info from package.json synchronously
 *
 * @returns {Record<string, any>} The full path to module's package.json and its payload
 * @throws {Error} If package.json cannot be found
 */
export function getPackageInfoSync(): {manifestPath: string; manifestPayload: Record<string, any>} {
  if (PACKAGE_INFO) {
    return PACKAGE_INFO;
  }

  let currentDir = path.dirname(path.resolve(__filename));
  let isAtFsRoot = false;
  while (!isAtFsRoot) {
    const manifestPath = path.join(currentDir, 'package.json');
    try {
      if (_fs.existsSync(manifestPath)) {
        const manifestPayload = JSON.parse(_fs.readFileSync(manifestPath, 'utf8'));
        if (manifestPayload.name === MODULE_NAME) {
          PACKAGE_INFO = {
            manifestPath,
            manifestPayload,
          };
          return PACKAGE_INFO;
        }
      }
    } catch {}
    currentDir = path.dirname(currentDir);
    isAtFsRoot = currentDir.length <= path.dirname(currentDir).length;
  }
  throw new Error(`Cannot find the root folder of the ${MODULE_NAME} Node.js module`);
}
