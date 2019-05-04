import _ from 'lodash';

function qualifyActivityName (activityName, packageName) {
  return _.startsWith(activityName, '.') ? `${packageName}${activityName}` : activityName;
}

export { qualifyActivityName };
