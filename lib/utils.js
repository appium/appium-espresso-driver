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
 * Recursively copy all files except build directories contents
 * @param sourceBaseDir {string} directory to copy files from
 * @param targetBaseDir {string} directory to copy files to
 */
async function copyGradleProjectRecursively (sourceBaseDir, targetBaseDir) {
  await fs.walkDir(sourceBaseDir, true, async (itemPath, isDirectory) => {
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

export { qualifyActivityName, copyGradleProjectRecursively };
