import _ from 'lodash';
import {errors} from 'appium/driver';
import {util} from 'appium/support';
import type {EspressoDriver} from '../driver';

/**
 * Flash the element with given id.
 * durationMillis and repeatCount are optional
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-flashelement
 * @param elementId - The ID of the element to flash
 * @param durationMillis - Optional duration in milliseconds for each flash
 * @param repeatCount - Optional number of times to repeat the flash
 * @returns Promise that resolves when the flash command is executed
 */
export async function mobileFlashElement(
  this: EspressoDriver,
  elementId: string,
  durationMillis?: number,
  repeatCount?: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${elementId}/flash`, 'POST', {
    durationMillis,
    repeatCount,
  });
}

/**
 * Dismisses the autofill UI for the given element.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-dismissautofill
 * @param elementId - The ID of the element for which to dismiss autofill
 * @returns Promise that resolves when the autofill is dismissed
 */
export async function mobileDismissAutofill(
  this: EspressoDriver,
  elementId: string,
): Promise<void> {
  await this.espresso.jwproxy.command(
    `/session/:sessionId/appium/execute_mobile/${elementId}/dismiss_autofill`,
    'POST',
    {},
  );
}

/**
 * Performs a swipe gesture on the given element.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-swipe
 * @param elementId - The ID of the element to swipe
 * @param direction - Optional swipe direction (e.g., 'up', 'down', 'left', 'right')
 * @param swiper - Optional swiper configuration
 * @param startCoordinates - Optional starting coordinates for the swipe
 * @param endCoordinates - Optional ending coordinates for the swipe
 * @param precisionDescriber - Optional precision describer for the swipe action
 * @returns Promise that resolves when the swipe is completed
 */
export async function mobileSwipe(
  this: EspressoDriver,
  elementId: string,
  direction?: string,
  swiper?: string,
  startCoordinates?: string,
  endCoordinates?: string,
  precisionDescriber?: string,
): Promise<any> {
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${elementId}/swipe`, 'POST', {
    direction,
    element: elementId,
    swiper,
    startCoordinates,
    endCoordinates,
    precisionDescriber,
  });
}

/**
 * Opens a drawer element (e.g., Navigation Drawer) with the specified gravity.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-opendrawer
 * @param elementId - The ID of the drawer element to open
 * @param gravity - Optional gravity value for drawer positioning (e.g., Gravity.START, Gravity.END)
 * @returns Promise that resolves when the drawer is opened
 */
export async function mobileOpenDrawer(
  this: EspressoDriver,
  elementId: string,
  gravity?: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/open_drawer`,
    'POST',
    {gravity},
  );
}

/**
 * Closes a drawer element (e.g., Navigation Drawer) with the specified gravity.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-closedrawer
 * @param elementId - The ID of the drawer element to close
 * @param gravity - Optional gravity value for drawer positioning (e.g., Gravity.START, Gravity.END)
 * @returns Promise that resolves when the drawer is closed
 */
export async function mobileCloseDrawer(
  this: EspressoDriver,
  elementId: string,
  gravity?: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/close_drawer`,
    'POST',
    {gravity},
  );
}

/**
 * Sets the date on a date picker element.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-setdate
 * @param elementId - The ID of the date picker element
 * @param year - The year to set (e.g., 2024)
 * @param monthOfYear - The month to set (0-11, where 0 is January)
 * @param dayOfMonth - The day of the month to set (1-31)
 * @returns Promise that resolves when the date is set
 */
export async function mobileSetDate(
  this: EspressoDriver,
  elementId: string,
  year: number,
  monthOfYear: number,
  dayOfMonth: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/set_date`,
    'POST',
    {
      year,
      monthOfYear,
      dayOfMonth,
    },
  );
}

/**
 * Sets the time on a time picker element.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-settime
 * @param elementId - The ID of the time picker element
 * @param hours - The hour to set (0-23)
 * @param minutes - The minute to set (0-59)
 * @returns Promise that resolves when the time is set
 */
export async function mobileSetTime(
  this: EspressoDriver,
  elementId: string,
  hours: number,
  minutes: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/set_time`,
    'POST',
    {
      hours,
      minutes,
    },
  );
}

/**
 * Navigates to a menu item in a navigation element (e.g., BottomNavigationView).
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-navigateto
 * @param elementId - The ID of the navigation element
 * @param menuItemId - The ID of the menu item to navigate to (must be a non-negative number)
 * @returns Promise that resolves when navigation is completed
 * @throws {errors.InvalidArgumentError} If menuItemId is not a non-negative number
 */
export async function mobileNavigateTo(
  this: EspressoDriver,
  elementId: string,
  menuItemId: number | string,
): Promise<any> {
  const menuItemIdAsNumber = parseInt(`${menuItemId}`, 10);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    throw new errors.InvalidArgumentError(
      `'menuItemId' must be a non-negative number. Found ${menuItemId}`,
    );
  }
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/navigate_to`,
    'POST',
    {menuItemId},
  );
}

/**
 * Perform a 'GeneralClickAction' (https://developer.android.com/reference/androidx/test/espresso/action/GeneralClickAction)
 *
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-clickaction
 * @param elementId - The ID of the element to perform the click action on
 * @param tapper - Optional tapper configuration for the click action
 * @param coordinatesProvider - Optional coordinates provider for the click position
 * @param precisionDescriber - Optional precision describer for the click action
 * @param inputDevice - Optional input device identifier
 * @param buttonState - Optional button state for the click action
 * @returns Promise that resolves when the click action is performed
 */
export async function mobileClickAction(
  this: EspressoDriver,
  elementId: string,
  tapper?: string,
  coordinatesProvider?: string,
  precisionDescriber?: string,
  inputDevice?: number,
  buttonState?: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/click_action`,
    'POST',
    {
      tapper,
      coordinatesProvider,
      precisionDescriber,
      inputDevice,
      buttonState,
    },
  );
}

/**
 * Scrolls to a specific page in a ViewPager or similar scrollable element.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-scrolltopage
 * @param elementId - The ID of the scrollable element (e.g., ViewPager)
 * @param scrollTo - Optional direction to scroll: 'first', 'last', 'left', or 'right'
 * @param scrollToPage - Optional page index to scroll to (must be non-negative)
 * @param smoothScroll - Optional flag to enable smooth scrolling (default: false)
 * @returns Promise that resolves when scrolling is completed
 * @throws {errors.InvalidArgumentError} If scrollTo is not one of the valid values or scrollToPage is negative
 */
export async function mobileScrollToPage(
  this: EspressoDriver,
  elementId: string,
  scrollTo?: string,
  scrollToPage?: number,
  smoothScroll?: boolean,
): Promise<any> {
  const scrollToTypes = ['first', 'last', 'left', 'right'];
  if (scrollTo && !_.includes(scrollToTypes, scrollTo)) {
    throw new errors.InvalidArgumentError(
      `"scrollTo" must be one of "${scrollToTypes.join(', ')}" found '${scrollTo}'`,
    );
  }
  if ((scrollToPage ?? 0) < 0) {
    throw new errors.InvalidArgumentError(
      `"scrollToPage" must be a non-negative integer. Found '${scrollToPage}'`,
    );
  }
  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    this.log.warn(`'scrollTo' and 'scrollToPage' where both provided. Defaulting to 'scrollTo'`);
  }

  return await this.espresso.jwproxy.command(
    `/appium/execute_mobile/${elementId}/scroll_to_page`,
    'POST',
    {
      scrollTo,
      scrollToPage,
      smoothScroll,
    },
  );
}

/**
 * Performs an editor action (e.g., IME action like DONE, SEARCH, NEXT) on the current input field.
 * @param action - The editor action to perform (can be a string or numeric action code)
 * @returns Promise that resolves when the editor action is performed
 */
export async function mobilePerformEditorAction(
  this: EspressoDriver,
  action: string | number,
): Promise<void> {
  await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
}
