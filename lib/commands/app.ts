import type {CachedAppInfo} from '@appium/types';
import {isPlainObject} from '../utils';

/** Cached app entry with integrity path (narrower than {@link CachedAppInfo}). */
export type StrictCachedAppInfo = CachedAppInfo & {fullPath: string};

/** Type guard for cached app metadata shape. */
export function isCachedAppInfo(value: unknown): value is StrictCachedAppInfo {
  return (
    isPlainObject(value) &&
    typeof value.packageHash === 'string' &&
    typeof value.fullPath === 'string'
  );
}
