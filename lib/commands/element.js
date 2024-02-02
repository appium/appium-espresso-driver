import _ from 'lodash';
import { errors } from 'appium/driver';
import { util } from 'appium/support';
import { requireOptions } from '../utils';

/**
 * Flash the element with given id.
 * durationMillis and repeatCount are optional
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileFlashElement (opts = {}) {
  const {durationMillis, repeatCount} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/flash`,
    'POST', {
      durationMillis,
      repeatCount
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileDismissAutofill (opts = {}) {
  await this.espresso.jwproxy.command(
    `/session/:sessionId/appium/execute_mobile/${requireElementId(opts)}/dismiss_autofill`,
    'POST',
    {}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileSwipe (opts = {}) {
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
}


/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileOpenDrawer (opts = {}) {
  const {gravity} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/open_drawer`,
    'POST',
    {gravity}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileCloseDrawer (opts = {}) {
  const {gravity} = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/close_drawer`,
    'POST',
    {gravity}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileSetDate (opts = {}) {
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
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileSetTime (opts = {}) {
  const {hours, minutes} = requireOptions(opts, ['hours', 'minutes']);
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/set_time`,
    'POST', {
      hours,
      minutes,
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileNavigateTo (opts = {}) {
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
}

/**
 * Perform a 'GeneralClickAction' (https://developer.android.com/reference/androidx/test/espresso/action/GeneralClickAction)
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileClickAction (opts = {}) {
  const {
    tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
  } = opts;
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${requireElementId(opts)}/click_action`,
    'POST', {
      tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
    }
  );
}

/**
 *
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileScrollToPage (opts = {}) {
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
}

/**
 * @typedef {Object} PerformEditorActionOpts
 * @property {string|number} action
 */

/**
 * @this {import('../driver').EspressoDriver}
 * @param {PerformEditorActionOpts} opts
 * @returns {Promise<void>}
 */
export async function mobilePerformEditorAction (opts) {
  const {action} = requireOptions(opts, ['action']);
  await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
}

// #region Internal Helpers

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

// #endregion
