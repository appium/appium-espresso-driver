
/**
 * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
 *
 * @param {string} activityName
 * @param {string} packageName
 * @returns {string} The qualified activity name
 */
function qualifyActivityName (activityName, packageName) {
  if (!activityName || !packageName) {
    return activityName;
  }

  const dotPos = activityName.indexOf('.');
  if (dotPos > 0) {
    return activityName;
  }
  return `${packageName}${dotPos === 0 ? '' : '.'}${activityName}`;
}

export { qualifyActivityName };
