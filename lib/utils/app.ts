import {isPlainObject} from './predicates';

export interface CachedAppInfo {
  packageHash: string;
  fullPath: string;
}

/**
 * https://android.googlesource.com/platform/frameworks/base/+/master/tools/aapt/Resource.cpp#755
 *
 * @param activityName
 * @param packageName
 * @returns The qualified activity name
 */
export function qualifyActivityName(activityName: string, packageName: string): string {
  // if either activity or package name is not set
  // or any of these contain wildcards then there is
  // no point in qualifying the activity name
  if ([activityName, packageName].some((name) => !name || name.includes('*'))) {
    return activityName;
  }

  const dotPos = activityName.indexOf('.');
  if (dotPos > 0) {
    return activityName;
  }
  return `${packageName}${dotPos === 0 ? '' : '.'}${activityName}`;
}

/** Type guard for cached app metadata shape. */
export function isCachedAppInfo(value: unknown): value is CachedAppInfo {
  return (
    isPlainObject(value) &&
    typeof value.packageHash === 'string' &&
    typeof value.fullPath === 'string'
  );
}
