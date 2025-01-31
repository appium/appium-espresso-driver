import AndroidDriver from 'appium-android-driver';
import { qualifyActivityName, requireOptions } from '../utils';

/**
 * @typedef {Object} BackgroundAppOptions
 * @property {number} [seconds] The amount of seconds to wait between putting the app to background and restoring it.
 * Any negative value means to not restore the app after putting it to the background (the default behavior).
 */

/**
 * Puts the app under test to the background
 * and then restores it (if needed). The call is blocking is the
 * app needs to be restored afterwards.
 *
 * @this {import('../driver').EspressoDriver}
 * @param {BackgroundAppOptions} [opts={}]
 */
export async function mobileBackgroundApp (opts = {}) {
  const {seconds = -1} = opts;
  // @ts-ignore
  return await this.background(seconds);
}

/**
 * @overload
 * @typedef {Object} StartActivityOptions
 * @property {string} appActivity
 * @property {string} [locale]
 * @property {string} [optionalIntentArguments]
 * @property {string} [optionalActivityArguments]
 */

/**
 * Starts the given activity with intent options, activity options and locale.
 * Activity could only be executed in scope of the current app package.
 *
 * @this {import('../driver').EspressoDriver}
 * @overload
 * @param {StartActivityOptions} opts
 * @returns {Promise<string>}
 */
export async function mobileStartActivity (opts) {
  const appPackage = this.caps.appPackage;
  const {
    appActivity,
    locale,
    optionalIntentArguments,
    optionalActivityArguments
  } = requireOptions(opts, ['appActivity']);
  return /** @type {string} */ (await this.espresso.jwproxy.command(`/appium/device/start_activity`, 'POST', {
    appPackage,
    appActivity,
    locale,
    optionalIntentArguments,
    optionalActivityArguments
  }));
}

/**
 *
 * @this {import('../driver').EspressoDriver}
 * @param {string} appPackage
 * @param {string} appActivity
 * @param {string} appWaitPackage
 * @param {string} appWaitActivity
 */
export async function startActivity (
  appPackage, appActivity, appWaitPackage, appWaitActivity
) {
  // intentAction, intentCategory, intentFlags, optionalIntentArguments, dontStopAppOnReset
  // parameters are not supported by Espresso
  const pkg = /** @type {string} */ (appPackage || this.caps.appPackage);
  const appWaitPkg = /** @type {string} */ (appWaitPackage || pkg);
  const appAct = qualifyActivityName(appActivity, pkg);
  const appWaitAct = qualifyActivityName(appWaitActivity || appAct, appWaitPkg);
  this.log.debug(`Starting activity '${appActivity}' for package '${appPackage}'`);
  await this.espresso.jwproxy.command(`/appium/device/start_activity`, 'POST', {
    appPackage: pkg,
    appActivity: appAct,
  });
  await this.adb.waitForActivity(appWaitPkg, appWaitAct);
};
