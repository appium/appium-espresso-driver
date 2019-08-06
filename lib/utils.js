import _ from 'lodash';

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

export { qualifyActivityName };
