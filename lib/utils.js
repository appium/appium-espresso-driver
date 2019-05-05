import _ from 'lodash';

function qualifyActivityName (activityName, packageName) {
  return _.startsWith(activityName, '.') && packageName
    ? `${packageName}${activityName}`
    : activityName;
}

export { qualifyActivityName };
