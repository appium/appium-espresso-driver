import { qualifyActivityName } from '../utils';

/**
 * Starts the given activity with intent options, activity options and locale.
 * Activity could only be executed in scope of the current app package.
 *
 * @this {import('../driver').EspressoDriver}
 * @param {string} appActivity
 * @param {string} [locale]
 * @param {string} [optionalIntentArguments]
 * @param {string} [optionalActivityArguments]
 * @returns {Promise<string>}
 */
export async function mobileStartActivity (
  appActivity,
  locale,
  optionalIntentArguments,
  optionalActivityArguments
) {
  const appPackage = this.caps.appPackage;
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
