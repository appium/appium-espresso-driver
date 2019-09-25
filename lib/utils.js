import { fs, mkdirp } from 'appium-support';
import _ from 'lodash';
import path from 'path';

/**
 * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
 *
 * @param {string} activityName
 * @param {string} packageName
 * @returns {string} The qualified activity name
 */
function qualifyActivityName (activityName, packageName) {
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
 * Recursively copy all files
 * @param sourceBaseDir {string} directory to copy files from
 * @param targetBaseDir {string} directory to copy files to
 */
async function copyRecursively (sourceBaseDir, targetBaseDir) {
  await fs.walkDir(sourceBaseDir, true, async (itemPath, isDirectory) => {
    const relativePath = path.relative(sourceBaseDir, itemPath);
    const targetPath = path.resolve(targetBaseDir, relativePath);

    const isGradleBuildDir = `${path.sep}${itemPath}${path.sep}`.includes(`${path.sep}build${path.sep}`);
    if (isGradleBuildDir) {
      return false;
    }

    if (!isDirectory) {
      await fs.copyFile(itemPath, targetPath);
    } else {
      await mkdirp(targetPath);
    }
    return false;
  });
}

export { qualifyActivityName, copyRecursively };
