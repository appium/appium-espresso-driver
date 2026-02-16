import type {EspressoDriver} from '../driver';

/**
 * Registers one or more idling resources
 *
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-registeridlingresources
 * @param classNames - The comma-separated list of idling resources class names.
 * Each name must be a full-qualified java class name, like `io.appium.espressoserver.lib.MyIdlingResource`.
 * Each class in the app source must implement a singleton pattern and have a static `getInstance()`
 * method returning the class instance, which implements `androidx.test.espresso.IdlingResource`
 * interface. Read
 * - https://developer.android.com/training/testing/espresso/idling-resource
 * - https://android.jlelse.eu/integrate-espresso-idling-resources-in-your-app-to-build-flexible-ui-tests-c779e24f5057
 * for more details on how to design and use idling resources concept in Espresso.
 * @returns Promise that resolves when idling resources are registered
 * @throws {Error} If there was a failure while parsing options or registering
 * the actual instances
 */
export async function mobileRegisterIdlingResources(
  this: EspressoDriver,
  classNames: string,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    '/appium/execute_mobile/register_idling_resources',
    'POST',
    {
      classNames,
    },
  );
}

/**
 * Unregisters one or more idling resources
 *
 * @param classNames - The comma-separated list of idling resources class names.
 * Each name must be a full-qualified java class name, like `io.appium.espressoserver.lib.MyIdlingResource`.
 * Each class in the app source must implement a singleton pattern and have a static `getInstance()`
 * method returning the class instance, which implements `androidx.test.espresso.IdlingResource`
 * interface. Read
 * - https://developer.android.com/training/testing/espresso/idling-resource
 * - https://android.jlelse.eu/integrate-espresso-idling-resources-in-your-app-to-build-flexible-ui-tests-c779e24f5057
 * for more details on how to design and use idling resources concept in Espresso.
 * @returns Promise that resolves when idling resources are unregistered
 * @throws {Error} If there was a failure while parsing options or unregistering
 * the actual instances
 */
export async function mobileUnregisterIdlingResources(
  this: EspressoDriver,
  classNames: string,
): Promise<any> {
  return await this.espresso.jwproxy.command(
    '/appium/execute_mobile/unregister_idling_resources',
    'POST',
    {
      classNames,
    },
  );
}

/**
 * Returns a list of currently registered idling resources
 * or an empty list if no resources have been registered yet.
 *
 * @returns Promise that resolves to the list of fully qualified class names
 */
export async function mobileListIdlingResources(this: EspressoDriver): Promise<string[]> {
  return (await this.espresso.jwproxy.command(
    '/appium/execute_mobile/list_idling_resources',
    'GET',
  )) as string[];
}

/**
 * Wait for UI thread to be idle.
 * @returns Promise that resolves when the UI thread is idle
 */
export async function mobileWaitForUIThread(this: EspressoDriver): Promise<any> {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/ui_thread_sync', 'POST');
}
