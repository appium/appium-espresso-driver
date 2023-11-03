import _ from 'lodash';
import { util } from 'appium/support';
import { errors } from 'appium/driver';
import { qualifyActivityName } from '../utils';

const commands = {};

/**
 * @template {Record<string, any>} T
 * @param {T} options
 * @param {string[]|string} requiredOptionNames
 * @returns {T}
 */
function requireOptions (options, requiredOptionNames) {
  if (!_.isArray(requiredOptionNames)) {
    requiredOptionNames = [requiredOptionNames];
  }
  const presentOptionNames = _.keys(options);
  const missingOptionNames = _.difference(requiredOptionNames, presentOptionNames);
  if (_.isEmpty(missingOptionNames)) {
    return options;
  }
  throw new Error(`The following options are required: ${JSON.stringify(missingOptionNames)}. ` +
    `You have only provided: ${JSON.stringify(presentOptionNames)}`);
}

/**
 * @param {Record<string, any>} opts
 * @returns {string}
 */
function requireElementId (opts) {
  const {element, elementId} = opts;
  if (!element && !elementId) {
    throw new errors.InvalidArgumentError('Element Id must be provided');
  }
  return util.unwrapElement(elementId || element);
}

/**
 * @this {import('../driver').EspressoDriver}
 */
// eslint-disable-next-line require-await
commands.launchApp = async function launchApp () {
  throw new errors.UnsupportedOperationError('Please create a new session in order to launch the application under test');
};

/**
 * @this {import('../driver').EspressoDriver}
 */
// eslint-disable-next-line require-await
commands.closeApp = async function closeApp () {
  throw new errors.UnsupportedOperationError('Please quit the session in order to close the application under test');
};

/**
 * @this {import('../driver').EspressoDriver}
 */
// eslint-disable-next-line require-await
commands.reset = async function reset () {
  throw new errors.UnsupportedOperationError(
    'Please quit the session and create a new one ' +
    'in order to close and launch the application under test');
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.getClipboard = async function getClipboard () {
  return (await this.adb.getApiLevel() < 29)
    ? (await this.espresso.jwproxy.command('/appium/device/get_clipboard', 'POST', {}))
    : (await this.adb.getClipboard());
};

/**
 * @typedef {Object} PerformEditorActionOpts
 * @property {string|number} action
 */

/**
 * @this {import('../driver').EspressoDriver}
 * @param {PerformEditorActionOpts} opts
 * @returns {Promise<void>}
 */
commands.mobilePerformEditorAction = async function mobilePerformEditorAction (opts) {
  const {action} = requireOptions(opts, ['action']);
  await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileSwipe = async function mobileSwipe (opts = {}) {
  const {direction, swiper, startCoordinates, endCoordinates, precisionDescriber} = opts;
  const element = requireElementId(opts);
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${element}/swipe`,
    'POST',
    {
      direction,
      element,
      swiper,
      startCoordinates,
      endCoordinates,
      precisionDescriber,
    }
  );
};

/**
 * @typedef {Object} PressKeyOptions
 * @property {number} [keycode] A valid Android key code. See https://developer.android.com/reference/android/view/KeyEvent
 * for the list of available key codes
 * @property {number} [metastate] An integer in which each bit set to 1 represents a pressed meta key. See
 * https://developer.android.com/reference/android/view/KeyEvent for more details.
 * @property {number} [flags] Flags for the particular key event. See
 * https://developer.android.com/reference/android/view/KeyEvent for more details.
 * @property {boolean} [isLongPress] [false] Whether to emulate long key press
 */

/**
 * Emulates key press event.
 *
 * @this {import('../driver').EspressoDriver}
 * @param {PressKeyOptions} [opts={}]
 */
commands.mobilePressKey = async function mobilePressKey (opts = {}) {
  const {keycode, metastate, flags, isLongPress} = requireOptions(opts, ['keycode']);
  await this.espresso.jwproxy.command(`/appium/device/${isLongPress ? 'long_' : ''}press_keycode`, 'POST', {
    keycode, metastate, flags
  });
};

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
commands.mobileBackgroundApp = async function mobileBackgroundApp (opts = {}) {
  const {seconds = -1} = opts;
  return await this.background(seconds);
};

/**
 * @this {import('../driver').EspressoDriver}
 * @returns {Promise<import('../types').DeviceInfo>}
 */
commands.mobileGetDeviceInfo = async function mobileGetDeviceInfo () {
  return /** @type {import('../types').DeviceInfo} */ (await this.espresso.jwproxy.command('/appium/device/info', 'GET'));
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileIsToastVisible = async function mobileIsToastVisible (opts = {}) {
  const {text, isRegexp} = opts;
  if (!util.hasValue(text)) {
    throw new errors.InvalidArgumentError(`'text' argument is mandatory`);
  }
  return await this.espresso.jwproxy.command('/appium/execute_mobile/is_toast_displayed', 'POST', {
    text,
    isRegexp,
  });
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileOpenDrawer = async function mobileOpenDrawer (opts = {}) {
  const {gravity} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/open_drawer`,
    'POST',
    {gravity}
  );
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileCloseDrawer = async function mobileCloseDrawer (opts = {}) {
  const {gravity} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/close_drawer`,
    'POST',
    {gravity}
  );
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileSetDate = async function mobileSetDate (opts = {}) {
  const {year, monthOfYear, dayOfMonth} = requireOptions(
    opts, ['year', 'monthOfYear', 'dayOfMonth']
  );
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/set_date`,
    'POST', {
      year,
      monthOfYear,
      dayOfMonth,
    }
  );
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileSetTime = async function mobileSetTime (opts = {}) {
  const {hours, minutes} = requireOptions(opts, ['hours', 'minutes']);
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/set_time`,
    'POST', {
      hours,
      minutes,
    }
  );
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileNavigateTo = async function mobileNavigateTo (opts = {}) {
  const {menuItemId} = requireOptions(opts, ['menuItemId']);
  const menuItemIdAsNumber = parseInt(menuItemId, 10);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    throw new errors.InvalidArgumentError(
      `'menuItemId' must be a non-negative number. Found ${menuItemId}`
    );
  }
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/navigate_to`,
    'POST',
    {menuItemId}
  );
};

/**
 * Runs a chain of Espresso web atoms (see https://developer.android.com/training/testing/espresso/web for reference)
 *
 * Takes JSON of the form
 *
 * {
 *   "webviewEl": "<ELEMENT_ID>", // optional webview element to operate on
 *   "forceJavascriptEnabled": true|false, // if webview disables javascript, webatoms won't work, this forces it
 *   "methodChain": [
 *     {"name": "methodName", "atom": {"name": "atomName", "args": ["arg1", "arg2", ...]}},
 *     ...
 *   ]
 * }
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileWebAtoms = async function mobileWebAtoms (opts = {}) {
  opts = requireOptions(opts, ['methodChain']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/web_atoms`, 'POST', opts);
};

/**
 * @this {import('../driver').EspressoDriver}
 * @returns {Promise<number>}
 */
commands.getDisplayDensity = async function getDisplayDensity () {
  return /** @type {number} */ (await this.espresso.jwproxy.command(
    '/appium/device/display_density', 'GET', {})
  );
};

/**
 *
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileScrollToPage = async function mobileScrollToPage (opts = {}) {
  const {scrollTo, scrollToPage, smoothScroll} = opts;
  const scrollToTypes = ['first', 'last', 'left', 'right'];
  if (!scrollToTypes.includes(scrollTo)) {
    throw new errors.InvalidArgumentError(
      `"scrollTo" must be one of "${scrollToTypes.join(', ')}" found '${scrollTo}'`
    );
  }
  if (!_.isInteger(scrollToPage) || scrollToPage < 0) {
    throw new errors.InvalidArgumentError(
      `"scrollToPage" must be a non-negative integer. Found '${scrollToPage}'`
    );
  }
  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    this.log.warn(`'scrollTo' and 'scrollToPage' where both provided. Defaulting to 'scrollTo'`);
  }

  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/scroll_to_page`,
    'POST', {
      scrollTo,
      scrollToPage,
      smoothScroll,
    }
  );
};


/**
 *  API to invoke methods defined in Android app.
 *
 *  Example data
 *  {
 *   target: 'activity',
 *   methods:
 *         [
 *           {
 *               name: "someMethod",
 *           },
 *           {
 *               name: "anotherMethod",
 *               args:
 *                   [
 *                       {value: "Lol", type: 'java.lang.CharSequence'},
 *                       {value: 1, type: 'int'}
 *                   ]
 *           }
 *         ]
 * }
 *
 * In above example, method "someMethod" will be invoked on 'activity'. On the result, "anotherMethod" will be invoked
 *  "target" can be either 'activity', 'application' or 'element'
 *  If target is set to 'application', methods will be invoked on application class
 *  If target is set to 'activity', methods will be invoked on current activity
 *  If target is set to 'element', 'elementId' must be specified
 *
 * - Only 'Public' methods can be invoked. ('open' modifire is necessary in Kotlin)
 * - following primitive types are supported: "int", "boolean", "byte", "short", "long", "float", "char"
 * -  Non-primitive types with fully qualified name "java.lang.*" is also supported:
 *                              Eg. "java.lang.CharSequence", "java.lang.String", "java.lang.Integer", "java.lang.Float",
 *                              "java.lang.Double", "java.lang.Boolean", "java.lang.Long", "java.lang.Short",
 *                              "java.lang.Character" etc...
 *
 *
 * @throws {Error} if target is not 'activity' or 'application'
 * @throws {Error} if a method is not found with given argument types
 * @this {import('../driver').EspressoDriver}
 * @return {Promise<any>} the result of the last method in the invocation chain. If method return type is void, then "<VOID>" will be returned
 *
 */
commands.mobileBackdoor = async function mobileBackdoor (opts = {}) {
  const {target, methods} = requireOptions(opts, ['target', 'methods']);;
  if (target === 'element') {
    requireOptions(opts, ['elementId']);
  }
  const {elementId: targetElement} = opts;
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/backdoor`, 'POST', {target, methods, targetElement});
};

/**
 *  Execute UiAutomator2 commands to drive out of app areas.
 *  strategy can be one of: "clazz", "res", "text", "textContains", "textEndsWith", "textStartsWith",
 *                          "desc", "descContains", "descEndsWith", "descStartsWith", "pkg"
 *
 *  action can be one of: "click", "longClick", "getText", "getContentDescription", "getClassName",
 *                        "getResourceName", "getVisibleBounds", "getVisibleCenter", "getApplicationPackage",
 *                        "getChildCount", "clear", "isCheckable", "isChecked", "isClickable", "isEnabled",
 *                        "isFocusable", "isFocused", "isLongClickable", "isScrollable", "isSelected"
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileUiautomator = async function mobileUiautomator (opts = {}) {
  const {strategy, locator, action, index} = requireOptions(opts, ['strategy', 'locator', 'action']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/uiautomator`, 'POST', {strategy, locator, index, action});
};

/**
 * Execute UiAutomator2 command to return the UI dump when AUT is in background.
 * @this {import('../driver').EspressoDriver}
 * @throws  {Error} if uiautomator view dump is unsuccessful
 * @returns {Promise<string>} uiautomator DOM xml as string
 */
commands.mobileUiautomatorPageSource = async function mobileUiautomatorPageSource () {
  return /** @type {string} */ (await this.espresso.jwproxy.command(
    `/appium/execute_mobile/uiautomator_page_source`, 'GET'
  ));
};

/**
 * Flash the element with given id.
 * durationMillis and repeatCount are optional
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileFlashElement = async function mobileFlashElement (opts = {}) {
  const {durationMillis, repeatCount} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/flash`,
    'POST', {
      durationMillis,
      repeatCount
    }
  );
};

/**
 * Perform a 'GeneralClickAction' (https://developer.android.com/reference/androidx/test/espresso/action/GeneralClickAction)
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileClickAction = async function mobileClickAction (opts = {}) {
  const {
    tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
  } = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/click_action`,
    'POST', {
      tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
    }
  );
};

/**
 * @typedef {Object} SettingsOptions
 * @property {!string|number|boolean} Object Settings parameters that is available in
 * https://github.com/appium/appium-espresso-driver#settings-api or enabled plugins.
 */

/**
 * Apply the given settings to the espresso driver and the espresso server.
 * Errors by the espresso server will be printed as log, but it does not return an error message.
 * @param {SettingsOptions} settings
 * @this {import('../driver').EspressoDriver}
 */
commands.updateSettings = async function updateSettings (settings) {
  await this.settings.update(settings);
  try {
    await this.espresso.jwproxy.command(`/appium/settings`, 'POST', { settings });
  } catch (err) {
    this.log.warn(`The espresso driver responded an error. Original error: ${err.message}`);
  }
};

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.getSettings = async function getSettings () {
  const driverSettings = this.settings.getSettings();
  const serverSettings = /** @type {Record<String, any>} */ (await this.espresso.jwproxy.command(
    `/appium/settings`, 'GET'
  ));
  return {...driverSettings, ...serverSettings};
};

/**
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
 * @param {StartActivityOptions} opts
 * @returns {Promise<string>}
 */
commands.mobileStartActivity = async function mobileStartActivity (opts) {
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
};

/**
 *
 * @this {import('../driver').EspressoDriver}
 * @param {string} appPackage
 * @param {string} appActivity
 * @param {string} appWaitPackage
 * @param {string} appWaitActivity
 */
commands.startActivity = async function startActivity (
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

/**
 * @this {import('../driver').EspressoDriver}
 */
commands.mobileDismissAutofill = async function mobileDismissAutofill (opts = {}) {
  await this.espresso.jwproxy.command(
    `/session/:sessionId/appium/execute_mobile/${requireElementId(opts)}/dismiss_autofill`,
    'POST',
    {}
  );
};

export { commands };
export default commands;
