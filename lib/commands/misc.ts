import {util} from 'appium/support';
import {errors} from 'appium/driver';
import type {StringRecord} from '@appium/types';
import type {EspressoDriver} from '../driver';
import type {DeviceInfo} from '../types';

/**
 * Emulates key press event.
 *
 * @param keycode A valid Android key code. See https://developer.android.com/reference/android/view/KeyEvent
 * for the list of available key codes
 * @param metastate An integer in which each bit set to 1 represents a pressed meta key. See
 * https://developer.android.com/reference/android/view/KeyEvent for more details.
 * @param flags Flags for the particular key event. See
 * https://developer.android.com/reference/android/view/KeyEvent for more details.
 * @param isLongPress Whether to emulate long key press
 */
export async function mobilePressKey(
  this: EspressoDriver,
  keycode: number,
  metastate?: number,
  flags?: number,
  isLongPress: boolean = false,
): Promise<void> {
  await this.espresso.jwproxy.command(
    `/appium/device/${isLongPress ? 'long_' : ''}press_keycode`,
    'POST',
    {
      keycode,
      metastate,
      flags,
    },
  );
}

/**
 * Retrieves information about the connected Android device.
 * @returns Promise that resolves to device information including API version, platform version, manufacturer,
 * model, display size, and density
 */
export async function mobileGetDeviceInfo(this: EspressoDriver): Promise<DeviceInfo> {
  return (await this.espresso.jwproxy.command('/appium/device/info', 'GET')) as DeviceInfo;
}

/**
 * Checks if a toast message with the given text is currently visible on the screen.
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-istoastvisible
 * @param text - The text to search for in the toast message
 * @param isRegexp - Optional flag indicating if the text should be treated as a regular expression
 * @returns Promise that resolves to true if the toast is visible, false otherwise
 * @throws {errors.InvalidArgumentError} If text is not provided
 */
export async function mobileIsToastVisible(
  this: EspressoDriver,
  text: string,
  isRegexp?: boolean,
): Promise<any> {
  if (!util.hasValue(text)) {
    throw new errors.InvalidArgumentError(`'text' argument is mandatory`);
  }
  return await this.espresso.jwproxy.command('/appium/execute_mobile/is_toast_displayed', 'POST', {
    text,
    isRegexp,
  });
}

/**
 * Gets the display density (DPI) of the connected Android device.
 * @returns Promise that resolves to the display density value
 */
export async function getDisplayDensity(this: EspressoDriver): Promise<number> {
  return (await this.espresso.jwproxy.command(
    '/appium/device/display_density',
    'GET',
    {},
  )) as number;
}
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
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-backdoor
 * @param target - The target object to invoke methods on: 'activity', 'application', or 'element'
 * @param methods - Array of method definitions to invoke in sequence. Each method can have a name and optional args array
 * @param elementId - Required if target is 'element', the ID of the element to invoke methods on
 * @returns Promise that resolves to the result of the last method in the invocation chain.
 * If method return type is void, then "<VOID>" will be returned
 * @throws {errors.InvalidArgumentError} If target is 'element' but elementId is not provided
 * @throws {Error} If target is not 'activity', 'application', or 'element'
 * @throws {Error} If a method is not found with the given argument types
 */
export async function mobileBackdoor(
  this: EspressoDriver,
  target: string,
  methods: StringRecord[],
  elementId?: string,
): Promise<any> {
  if (target === 'element' && !elementId) {
    throw new errors.InvalidArgumentError(
      `'elementId' is required if 'target' equals to 'element'`,
    );
  }
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/backdoor`, 'POST', {
    target,
    methods,
    targetElement: elementId,
  });
}

/**
 *  Execute UiAutomator2 commands to drive out of app areas.
 *  strategy can be one of: "clazz", "res", "text", "textContains", "textEndsWith", "textStartsWith",
 *                          "desc", "descContains", "descEndsWith", "descStartsWith", "pkg"
 *
 *  action can be one of: "click", "longClick", "getText", "getContentDescription", "getClassName",
 *                        "getResourceName", "getVisibleBounds", "getVisibleCenter", "getApplicationPackage",
 *                        "getChildCount", "clear", "isCheckable", "isChecked", "isClickable", "isEnabled",
 *                        "isFocusable", "isFocused", "isLongClickable", "isScrollable", "isSelected"
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-uiautomator
 * @param strategy - The locator strategy to use (e.g., "clazz", "res", "text", "desc", "pkg", etc.)
 * @param locator - The locator value to match elements
 * @param action - The action to perform on the matched element
 * @param index - Optional zero-based index if multiple elements match the locator
 * @returns Promise that resolves to the result of the action (varies by action type)
 */
export async function mobileUiautomator(
  this: EspressoDriver,
  strategy: string,
  locator: string,
  action: string,
  index?: number,
): Promise<any> {
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/uiautomator`, 'POST', {
    strategy,
    locator,
    index,
    action,
  });
}

/**
 * Execute UiAutomator2 command to return the UI dump when AUT is in background.
 * @throws  {Error} if uiautomator view dump is unsuccessful
 * @returns {Promise<string>} uiautomator DOM xml as string
 */
export async function mobileUiautomatorPageSource(this: EspressoDriver): Promise<string> {
  return (await this.espresso.jwproxy.command(
    `/appium/execute_mobile/uiautomator_page_source`,
    'GET',
  )) as string;
}

/**
 * Settings parameters that is available in
 * https://github.com/appium/appium-espresso-driver#settings-api or enabled plugins.
 */
export interface SettingsOptions {
  [key: string]: string | number | boolean;
}

/**
 * Apply the given settings to the espresso driver and the espresso server.
 * Errors by the espresso server will be printed as log, but it does not return an error message.
 * @param settings - Settings object containing key-value pairs of settings to apply
 * @returns Promise that resolves when settings are updated
 */
export async function updateSettings(
  this: EspressoDriver,
  settings: SettingsOptions,
): Promise<void> {
  await this.settings.update(settings);
  try {
    await this.espresso.jwproxy.command(`/appium/settings`, 'POST', {settings});
  } catch (err: any) {
    this.log.warn(`The espresso driver responded an error. Original error: ${err.message}`);
  }
}

/**
 * Retrieves the current settings from both the driver and the espresso server.
 * @returns Promise that resolves to a merged object containing all current settings from both driver and server
 */
export async function getSettings(this: EspressoDriver): Promise<Record<string, any>> {
  const driverSettings = this.settings.getSettings();
  const serverSettings = (await this.espresso.jwproxy.command(`/appium/settings`, 'GET')) as Record<
    string,
    any
  >;
  return {...driverSettings, ...serverSettings};
}
