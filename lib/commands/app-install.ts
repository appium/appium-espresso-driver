import type {PostProcessOptions, PostProcessResult, CachedAppInfo} from '@appium/types';
import {utils} from 'appium-android-driver';
import {fs, tempDir, zip} from 'appium/support';
import {SETTINGS_HELPER_ID} from 'io.appium.settings';
import path from 'node:path';
import type {EspressoDriver} from '../driver';
import {TEST_APK_PKG} from './server';
import {isPlainObject} from '../utils';

export const APK_EXT = '.apk';
export const AAB_EXT = '.aab';
export const SUPPORTED_EXTENSIONS = [APK_EXT, AAB_EXT];

/** Cached app entry with integrity path (narrower than {@link CachedAppInfo}). */
export type StrictCachedAppInfo = CachedAppInfo & {fullPath: string};

/** Whether the session targets an app already installed on the device (no app path). */
export function isAppOnDevice(driver: EspressoDriver): boolean {
  const appPackage = driver.opts.appPackage;
  return !driver.opts.app && !!appPackage && driver.helpers.isPackageOrBundle(appPackage);
}

/**
 * Unzip the given app path and return the first package that has SUPPORTED_EXTENSIONS
 * in the archived file.
 *
 * @param appPath The path to app file.
 * @returns The path to an unzipped app file path.
 * @throws If the zip did not have any SUPPORTED_EXTENSIONS packages.
 */
export async function unzipApp(this: EspressoDriver, appPath: string): Promise<string> {
  const useSystemUnzipEnv = process.env.APPIUM_PREFER_SYSTEM_UNZIP;
  const useSystemUnzip =
    !useSystemUnzipEnv || !['0', 'false'].includes(useSystemUnzipEnv.toLowerCase());
  const tmpRoot = await tempDir.openDir();
  await zip.extractAllTo(appPath, tmpRoot, {useSystemUnzip});

  const globPattern = `**/*.+(${SUPPORTED_EXTENSIONS.map((ext) => ext.replace(/^\./, '')).join('|')})`;
  const sortedBundleItems = (
    await fs.glob(globPattern, {
      cwd: tmpRoot,
    })
  ).sort((a, b) => a.split(path.sep).length - b.split(path.sep).length);
  if (sortedBundleItems.length === 0) {
    throw this.log.errorWithException(
      `${this.opts.app} did not have any of '${SUPPORTED_EXTENSIONS.join(', ')}' ` +
        `extension packages. Please make sure the provided .zip archive contains at ` +
        `least one valid application package.`,
    );
  }
  const unzippedAppPath = path.join(tmpRoot, sortedBundleItems[0]);
  this.log.debug(`'${unzippedAppPath}' is the unzipped file from '${appPath}'`);
  return unzippedAppPath;
}

/** Post-processes configured apps and reuses a valid cache entry when possible. */
export async function onPostConfigureApp(
  this: EspressoDriver,
  opts: PostProcessOptions,
): Promise<PostProcessResult | undefined> {
  const {cachedAppInfo, isUrl, appPath} = opts;
  if (!appPath) {
    return undefined;
  }

  const presignApp = async (appLocation: string) => {
    if (this.opts.noSign) {
      this.log.info(
        'Skipping application signing because noSign capability is set to true. ' +
          'Having the application under test with improper signature/non-signed will cause ' +
          'Espresso automation startup failure.',
      );
    } else if (!(await this.adb.checkApkCert(appLocation))) {
      await this.adb.sign(appLocation);
    }
  };

  const hasApkExt = (p: string) => p.toLowerCase().endsWith(APK_EXT);
  const hasAabExt = (p: string) => p.toLowerCase().endsWith(AAB_EXT);
  const extractUniversalApk = async (shouldExtract: boolean, p: string) =>
    shouldExtract ? p : await this.adb.extractUniversalApk(p);

  let pathInCache: string | null = null;
  let isResultAppPathAlreadyCached = false;
  if (isCachedAppInfo(cachedAppInfo)) {
    const packageHash = await fs.hash(appPath);
    if (packageHash === cachedAppInfo.packageHash && (await fs.exists(cachedAppInfo.fullPath))) {
      this.log.info(`Using '${cachedAppInfo.fullPath}' which is cached from '${appPath}'`);
      isResultAppPathAlreadyCached = true;
      pathInCache = cachedAppInfo.fullPath;
    }
  }

  const isApk = hasApkExt(appPath);
  const shouldResultAppPathBeCached = !isApk || (isApk && isUrl);

  if (!isResultAppPathAlreadyCached) {
    if (shouldResultAppPathBeCached) {
      let unzippedAppPath: string | undefined;
      let isUnzippedApk = false;
      if (!(hasApkExt(appPath) || hasAabExt(appPath))) {
        unzippedAppPath = await this.unzipApp(appPath);
        isUnzippedApk = hasApkExt(unzippedAppPath);
      }

      pathInCache = unzippedAppPath
        ? await extractUniversalApk(isUnzippedApk, unzippedAppPath)
        : await extractUniversalApk(isApk, appPath);

      if (!isApk && isUrl) {
        await fs.rimraf(appPath);
      }
      if (unzippedAppPath !== undefined && hasAabExt(unzippedAppPath)) {
        await fs.rimraf(unzippedAppPath);
      }
      if (pathInCache == null) {
        throw this.log.errorWithException('Expected a cached app path after post-processing');
      }
      await presignApp(pathInCache);
    } else if (isApk) {
      await presignApp(appPath);
    }
  }
  return shouldResultAppPathBeCached && pathInCache != null ? {appPath: pathInCache} : undefined;
}

/** Prepare the application under test on the device. */
export async function initAUT(this: EspressoDriver): Promise<void> {
  if (this.opts.uninstallOtherPackages) {
    await this.uninstallOtherPackages(utils.parseArray(this.opts.uninstallOtherPackages), [
      SETTINGS_HELPER_ID,
      TEST_APK_PKG,
    ]);
  }

  if (!this.opts.app) {
    if (this.opts.fullReset) {
      throw this.log.errorWithException(
        'Full reset requires an app capability, use fastReset if app is not provided',
      );
    }
    this.log.debug('No app capability. Assuming it is already on the device');
    if (this.opts.fastReset) {
      await this.resetAUT();
    }
  }

  const appPackage = this.opts.appPackage;
  if (!appPackage) {
    throw this.log.errorWithException('appPackage is required');
  }

  if (!this.opts.skipUninstall) {
    await this.adb.uninstallApk(appPackage);
  }
  if (this.opts.app) {
    await this.installAUT();
  }
  if (this.opts.skipServerInstallation) {
    this.log.debug('skipServerInstallation capability is set. Not installig espresso-server');
  } else {
    await this.espresso.installTestApk();
  }

  try {
    await this.adb.addToDeviceIdleWhitelist(SETTINGS_HELPER_ID, TEST_APK_PKG);
  } catch (e: unknown) {
    const stderr =
      typeof e === 'object' &&
      e !== null &&
      'stderr' in e &&
      typeof (e as {stderr: unknown}).stderr === 'string'
        ? (e as {stderr: string}).stderr
        : undefined;
    const message = e instanceof Error ? e.message : String(e);
    this.log.warn(
      `Cannot add server packages to the Doze whitelist. Original error: ` + (stderr || message),
    );
  }
}

/** Type guard for cached app metadata shape. */
function isCachedAppInfo(value: unknown): value is StrictCachedAppInfo {
  return (
    isPlainObject(value) &&
    typeof value.packageHash === 'string' &&
    typeof value.fullPath === 'string'
  );
}
