import {ADB} from 'appium-adb';

/** @type {Promise<import('appium-adb').ADB> | undefined} */
let manifestAdbPromise;

/**
 * @param {string} apkPath
 * @returns {Promise<boolean | null>}
 */
export async function detectApkInternetPermission(apkPath) {
  try {
    const adb = await getManifestAdb();
    return await adb.hasInternetPermissionFromManifest(apkPath);
  } catch {
    return null;
  }
}

/** @returns {Promise<import('appium-adb').ADB>} */
async function getManifestAdb() {
  if (!manifestAdbPromise) {
    manifestAdbPromise = ADB.createADB({suppressKillServer: true});
  }
  return manifestAdbPromise;
}
