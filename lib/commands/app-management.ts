import type {EspressoDriver} from '../driver';
import {errors} from 'appium/driver';
import {qualifyActivityName} from '../utils';

/**
 * Starts the given activity with intent options, activity options and locale.
 * Activity could only be executed in scope of the current app package.
 *
 * @param appActivity - The activity name to start
 * @param locale - Optional locale string (e.g., 'en_US', 'fr_FR')
 * @param optionalIntentArguments - Optional intent arguments as a string
 * @param optionalActivityArguments - Optional activity arguments as a string
 * @returns Promise that resolves to a string when the activity is started
 */
export async function mobileStartActivity(
  this: EspressoDriver,
  appActivity: string,
  locale?: string,
  optionalIntentArguments?: string,
  optionalActivityArguments?: string,
): Promise<string> {
  const appPackage = this.caps.appPackage;
  return (await this.espresso.jwproxy.command(`/appium/device/start_activity`, 'POST', {
    appPackage,
    appActivity,
    locale,
    optionalIntentArguments,
    optionalActivityArguments,
  })) as string;
}

/**
 * Starts an activity with the given package and activity names.
 * Waits for the activity to be ready before returning.
 *
 * @param appPackage - The package name of the app to start
 * @param appActivity - The activity name to start
 * @param appWaitPackage - Optional package name to wait for (defaults to appPackage)
 * @param appWaitActivity - Optional activity name to wait for (defaults to appActivity)
 * @returns Promise that resolves when the activity is started and ready
 */
export async function startActivity(
  this: EspressoDriver,
  appPackage?: string,
  appActivity?: string,
  appWaitPackage?: string,
  appWaitActivity?: string,
): Promise<void> {
  // intentAction, intentCategory, intentFlags, optionalIntentArguments, dontStopAppOnReset
  // parameters are not supported by Espresso
  const pkg = appPackage || this.caps.appPackage;
  if (!pkg) {
    throw new errors.InvalidArgumentError('appPackage is required');
  }
  const appWaitPkg = appWaitPackage || pkg;
  if (!appActivity) {
    throw new errors.InvalidArgumentError('appActivity is required');
  }
  const appAct = qualifyActivityName(appActivity, pkg);
  const appWaitAct = qualifyActivityName(appWaitActivity || appAct, appWaitPkg);
  this.log.debug(`Starting activity '${appActivity}' for package '${appPackage}'`);
  await this.espresso.jwproxy.command(`/appium/device/start_activity`, 'POST', {
    appPackage: pkg,
    appActivity: appAct,
  });
  await this.adb.waitForActivity(appWaitPkg, appWaitAct);
}
