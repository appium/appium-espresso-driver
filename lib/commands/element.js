import _ from 'lodash';
import { errors } from 'appium/driver';
import { util } from 'appium/support';
import { requireOptions } from '../utils';

/**
 * Flash the element with given id.
 * durationMillis and repeatCount are optional
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-flashelement
 * @param {string} elementId
 * @param {number} durationMillis
 * @param {number} repeatCount
 */
export async function mobileFlashElement (
  elementId,
  durationMillis,
  repeatCount
) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/flash`,
    'POST', {
      durationMillis,
      repeatCount
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-dismissautofill
 * @param {string} elementId
 */
export async function mobileDismissAutofill (elementId) {
  await this.espresso.jwproxy.command(
    `/session/:sessionId/appium/execute_mobile/${elementId}/dismiss_autofill`,
    'POST',
    {}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-swipe
 * @param {string} elementId
 * @param {string} [direction]
 * @param {string} [swiper]
 * @param {string} [startCoordinates]
 * @param {string} [endCoordinates]
 * @param {string} [precisionDescriber]
 */
export async function mobileSwipe (
  elementId,
  direction,
  swiper,
  startCoordinates,
  endCoordinates,
  precisionDescriber
) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/swipe`,
    'POST',
    {
      direction,
      element: elementId,
      swiper,
      startCoordinates,
      endCoordinates,
      precisionDescriber,
    }
  );
}


/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-opendrawer
 * @param {string} elementId
 * @param {number} [gravity]
 */
export async function mobileOpenDrawer (elementId, gravity) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/open_drawer`,
    'POST',
    {gravity}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-closedrawer
 * @param {string} elementId
 * @param {number} [gravity]
 */
export async function mobileCloseDrawer (elementId, gravity) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/close_drawer`,
    'POST',
    {gravity}
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-setdate
 * @param {string} elementId
 * @param {number} year
 * @param {number} monthOfYear
 * @param {number} dayOfMonth
 */
export async function mobileSetDate (
  elementId,
  year,
  monthOfYear,
  dayOfMonth
) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/set_date`,
    'POST', {
      year,
      monthOfYear,
      dayOfMonth,
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-settime
 * @param {string} elementId
 * @param {number} hours
 * @param {number} minutes
 */
export async function mobileSetTime (
  elementId,
  hours,
  minutes
) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/set_time`,
    'POST', {
      hours,
      minutes,
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-navigateto
 * @param {string} elementId
 * @param {number | string} menuItemId
 */
export async function mobileNavigateTo (elementId, menuItemId) {
  const menuItemIdAsNumber = parseInt(`${menuItemId}`, 10);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    throw new errors.InvalidArgumentError(
      `'menuItemId' must be a non-negative number. Found ${menuItemId}`
    );
  }
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/navigate_to`,
    'POST',
    {menuItemId}
  );
}

/**
 * Perform a 'GeneralClickAction' (https://developer.android.com/reference/androidx/test/espresso/action/GeneralClickAction)
 *
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-clickaction
 * @this {import('../driver').EspressoDriver}
 * @param {string} elementId
 * @param {string} [tapper]
 * @param {string} [coordinatesProvider]
 * @param {string} [precisionDescriber]
 * @param {number} [inputDevice]
 * @param {number} [buttonState]
 */
export async function mobileClickAction (
  elementId,
  tapper,
  coordinatesProvider,
  precisionDescriber,
  inputDevice,
  buttonState
) {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/click_action`,
    'POST', {
      tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-scrolltopage
 * @param {string} elementId
 * @param {string} [scrollTo]
 * @param {number} [scrollToPage]
 * @param {boolean} [smoothScroll=false]
 */
export async function mobileScrollToPage (
  elementId,
  scrollTo,
  scrollToPage,
  smoothScroll,
) {
  const scrollToTypes = ['first', 'last', 'left', 'right'];
  if (!_.includes(scrollToTypes, scrollTo)) {
    throw new errors.InvalidArgumentError(
      `"scrollTo" must be one of "${scrollToTypes.join(', ')}" found '${scrollTo}'`
    );
  }
  if ((scrollToPage ?? 0) < 0) {
    throw new errors.InvalidArgumentError(
      `"scrollToPage" must be a non-negative integer. Found '${scrollToPage}'`
    );
  }
  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    this.log.warn(`'scrollTo' and 'scrollToPage' where both provided. Defaulting to 'scrollTo'`);
  }

  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/scroll_to_page`,
    'POST', {
      scrollTo,
      scrollToPage,
      smoothScroll,
    }
  );
}

/**
 * @this {import('../driver').EspressoDriver}
 * @param {string|number} action
 * @returns {Promise<void>}
 */
export async function mobilePerformEditorAction (action) {
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
