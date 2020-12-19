import { requireOptions } from '../utils';

const commands = {};

/**
 * @typedef {Object} IdlingResourcesOptions
 * @property {!string} classNames - The comma-separated list of idling resources class names.
 * Each name must be a full-qualified java class name, like `io.appium.espressoserver.lib.MyIdlingResource`.
 * Each class in the app source must implement a singleton pattern and have a static `getInstance()`
 * method returning the class instance, which implements `androidx.test.espresso.IdlingResource`
 * interface. Read
 * - https://developer.android.com/training/testing/espresso/idling-resource
 * - https://android.jlelse.eu/integrate-espresso-idling-resources-in-your-app-to-build-flexible-ui-tests-c779e24f5057
 * for more details on how to design and use idling resources concept in Espresso.
 */

/**
 * Registers one or more idling resources
 *
 * @param {IdlingResourcesOptions} opts
 * @throws {Error} If there was a failure while parsing options or registering
 * the actual instances
 */
commands.mobileRegisterIdlingResources = async function mobileRegisterIdlingResources (opts = {}) {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/register_idling_resources', 'POST',
    requireOptions(opts, ['classNames']));
};

/**
 * Unregisters one or more idling resources
 *
 * @param {IdlingResourcesOptions} opts
 * @throws {Error} If there was a failure while parsing options or unregistering
 * the actual instances
 */
commands.mobileUnregisterIdlingResources = async function mobileUnregisterIdlingResources (opts = {}) {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/unregister_idling_resources', 'POST',
    requireOptions(opts, ['classNames']));
};

/**
 * Returns a list of currently registered idling resources
 * or an empty list if no resources have been registered yet.
 *
 * @returns {Array<string>} The list of fully qualified class names
 */
commands.mobileListIdlingResources = async function mobileListIdlingResources () {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/list_idling_resources', 'GET');
};

export { commands };
export default commands;
