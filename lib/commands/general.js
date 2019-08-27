import _ from 'lodash';
import { util } from 'appium-support';
import logger from '../logger';
import validate from 'validate.js';
import { errors } from 'appium-base-driver';
import { qualifyActivityName } from '../utils';
import { androidHelpers } from 'appium-android-driver';

let commands = {}, helpers = {}, extensions = {};

function assertRequiredOptions (options, requiredOptionNames) {
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

// eslint-disable-next-line require-await
commands.launchApp = async function launchApp () {
  throw new errors.UnsupportedOperationError('Please create a new session in order to launch the application under test');
};

// eslint-disable-next-line require-await
commands.closeApp = async function closeApp () {
  throw new errors.UnsupportedOperationError('Please quit the session in order to close the application under test');
};

// eslint-disable-next-line require-await
commands.reset = async function reset () {
  throw new errors.UnsupportedOperationError('Please quit the session and create a new session in order to close and launch the application under test');
};

commands.mobilePerformEditorAction = async function mobilePerformEditorAction (opts = {}) {
  const {action} = assertRequiredOptions(opts, ['action']);
  return await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
};

commands.mobileSwipe = async function mobileSwipe (opts = {}) {
  const {direction, element, swiper, startCoordinates, endCoordinates, precisionDescriber} = assertRequiredOptions(opts, ['element']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/swipe`, 'POST', {
    direction, element, swiper, startCoordinates, endCoordinates, precisionDescriber
  });
};

commands.mobileGetDeviceInfo = async function mobileGetDeviceInfo () {
  return await this.espresso.jwproxy.command('/appium/device/info', 'GET');
};

commands.mobileIsToastVisible = async function mobileIsToastVisible (opts = {}) {
  const {text, isRegexp} = opts;
  if (!util.hasValue(text)) {
    logger.errorAndThrow(`'text' argument is mandatory`);
  }
  return await this.espresso.jwproxy.command('/appium/execute_mobile/is_toast_displayed', 'POST', {
    text,
    isRegexp,
  });
};

commands.mobileOpenDrawer = async function mobileOpenDrawer (opts = {}) {
  const {element, gravity} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/open_drawer`, 'POST', {
    gravity
  });
};

commands.mobileCloseDrawer = async function mobileCloseDrawer (opts = {}) {
  const {element, gravity} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/close_drawer`, 'POST', {
    gravity
  });
};

commands.mobileSetDate = async function mobileSetDate (opts = {}) {
  const {element, year, monthOfYear, dayOfMonth} = assertRequiredOptions(opts, ['element', 'year', 'monthOfYear', 'dayOfMonth']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_date`, 'POST', {
    year,
    monthOfYear,
    dayOfMonth,
  });
};

commands.mobileSetTime = async function mobileSetTime (opts = {}) {
  const {element, hours, minutes} = assertRequiredOptions(opts, ['element', 'hours', 'minutes']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_time`, 'POST', {
    hours,
    minutes,
  });
};

commands.mobileNavigateTo = async function mobileNavigateTo (opts = {}) {
  let {element, menuItemId} = assertRequiredOptions(opts, ['menuItemId', 'element']);

  let menuItemIdAsNumber = parseInt(menuItemId, 10);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    logger.errorAndThrow(`'menuItemId' must be a non-negative number. Found ${menuItemId}`);
    menuItemId = menuItemIdAsNumber;
  }

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/navigate_to`, 'POST', {
    menuItemId
  });
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
 *
 */
commands.mobileWebAtoms = async function mobileWebAtoms (opts = {}) {
  opts = assertRequiredOptions(opts, ['methodChain']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/web_atoms`, 'POST', opts);
};

commands.mobileScrollToPage = async function mobileScrollToPage (opts = {}) {

  // Validate the parameters
  const scrollToTypes = ['first', 'last', 'left', 'right'];
  const res = validate(opts, {
    element: {presence: true},
    scrollTo: {
      inclusion: {
        within: scrollToTypes,
        message: `"scrollTo" must be one of "${scrollToTypes.join(', ')}" found '%{value}'`,
      }
    },
    scrollToPage: {
      numericality: {
        onlyInteger: true,
        greaterThanOrEqualTo: 0,
        message: `"scrollToPage" must be a non-negative integer. Found '%{value}'`
      },
    },
  });

  if (util.hasValue(res)) {
    logger.errorAndThrow(`Invalid scrollTo parameters: ${JSON.stringify(res)}`);
  }

  const {element, scrollTo, scrollToPage, smoothScroll} = opts;

  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    logger.warn(`'scrollTo' and 'scrollToPage' where both provided. Defaulting to 'scrollTo'`);
  }

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/scroll_to_page`, 'POST', {
    scrollTo,
    scrollToPage,
    smoothScroll,
  });
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
 * @throws  {Error} if target is not 'activity' or 'application'
 * @throws  {Error} if a method is not found with given argument types
 *
 * @return {*} the result of the last method in the invocation chain. If method return type is void, then "<VOID>" will be returned
 *
 */
commands.mobileBackdoor = async function mobileBackdoor (opts = {}) {
  assertRequiredOptions(opts, ['target', 'methods']);
  const {target, methods} = opts;
  if (target === 'element') {
    assertRequiredOptions(opts, ['elementId']);
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
 */
commands.mobileUiautomator = async function mobileUiautomator (opts = {}) {
  const {strategy, locator, action, index} = assertRequiredOptions(opts, ['strategy', 'locator', 'action']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/uiautomator`, 'POST', {strategy, locator, index, action});
};

/**
 *  Flash the element with given id.
 *  durationMillis and repeatCount are optional
 *
 */
commands.mobileFlashElement = async function mobileFlashElement (opts = {}) {
  const {element} = assertRequiredOptions(opts, ['element']);
  const {durationMillis, repeatCount} = opts;
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/flash`, 'POST', {
    durationMillis,
    repeatCount
  });
};

/**
 * Perform a 'GeneralClickAction' (https://developer.android.com/reference/androidx/test/espresso/action/GeneralClickAction)
 */
commands.mobileClickAction = async function mobileClickAction (opts = {}) {
  const {element, tapper, coordinatesProvider, precisionDescriber,
         inputDevice, buttonState} = assertRequiredOptions(opts, ['element']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/click_action`, 'POST', {
    tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
  });
};

// eslint-disable-next-line require-await,no-unused-vars
commands.updateSettings = async function updateSettings (settings) {
  throw new errors.NotYetImplementedError();
};

// eslint-disable-next-line require-await
commands.getSettings = async function getSettings () {
  throw new errors.NotYetImplementedError();
};

// Stop proxying to any Chromedriver and redirect to Espresso
helpers.suspendChromedriverProxy = function suspendChromedriverProxy () {
  this.chromedriver = null;
  this.proxyReqRes = this.espresso.proxyReqRes.bind(this.espresso);
  this.jwpProxyActive = true;
};

commands.startActivity = async function startActivity (appPackage, appActivity,
  appWaitPackage, appWaitActivity) {
  // intentAction, intentCategory, intentFlags, optionalIntentArguments, dontStopAppOnReset
  // parameters are not supported by Espresso
  appPackage = appPackage || this.caps.appPackage;
  appWaitPackage = appWaitPackage || appPackage;
  appActivity = qualifyActivityName(appActivity, appPackage);
  appWaitActivity = qualifyActivityName(appWaitActivity || appActivity, appWaitPackage);
  logger.debug(`Starting activity '${appActivity}' for package '${appPackage}'`);
  await this.espresso.jwproxy.command(`/appium/device/start_activity`, 'POST', {
    appPackage,
    appActivity,
  });
  await this.adb.waitForActivity(appWaitPackage, appWaitActivity);
};

commands.reset = async function reset () {
  await androidHelpers.resetApp(this.adb, Object.assign({}, this.opts, {fastReset: true}));
  await this.espresso.startSession(this.caps);
  await this.adb.waitForActivity(this.caps.appWaitPackage, this.caps.appWaitActivity, this.opts.appWaitDuration);
  if (this.opts.autoWebview) {
    await this.initWebview();
  }
};

commands.mobileDismissAutofill = async function mobileDismissAutofill (opts = {}) {
  const {element} = assertRequiredOptions(opts, ['element']);
  await this.espresso.jwproxy.command(
    `/session/:sessionId/appium/execute_mobile/${util.unwrapElement(element)}/dismiss_autofill`, 'POST', {});
};

Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
