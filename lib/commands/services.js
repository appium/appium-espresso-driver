import { requireOptions } from '../utils';

const commands = {};


/**
 * @typedef {Object} StartServiceOptions
 * @property {!string} intent - The name of the service intent to start, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be started.
 * @property {boolean} foreground [false] - Set it to `true` if your service must be
 * started as foreground service.
 */

/**
 * Starts the given service intent.
 *
 * @param {StartServiceOptions} opts
 * @returns {string} The full component name
 * @throws {Error} If there was a failure while starting the service
 * or required options are missing
 */
commands.mobileStartService = async function mobileStartService (opts = {}) {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/start_service', 'POST',
    requireOptions(opts, ['intent']));
};

/**
 * @typedef {Object} StopServiceOptions
 * @property {!string} intent - The name of the service intent to stop, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be stopped.
 */

/**
 * Stops the given service intent.
 *
 * @param {StopServiceOptions} opts
 * @returns {string} `true` if the service has been successfully stopped
 * @throws {Error} If there was a failure while stopping the service
 * or required options are missing
 */
commands.mobileStopService = async function mobileStopService (opts = {}) {
  return await this.espresso.jwproxy.command('/appium/execute_mobile/stop_service', 'POST',
    requireOptions(opts, ['intent']));
};


export { commands };
export default commands;
