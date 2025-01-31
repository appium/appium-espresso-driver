/**
 * Starts the given service intent.
 *
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-startservice
 * @param {!string} intent - The name of the service intent to start, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be started.
 * @param {boolean} [foreground=false] - Set it to `true` if your service must be
 * started as foreground service.
 * @param {string | number} [user]
 * @returns {Promise<string>} The full component name
 * @throws {Error} If there was a failure while starting the service
 * or required options are missing
 */
export async function mobileStartService (intent, foreground, user) {
  return /** @type {string} */ (await this.espresso.jwproxy.command(
    '/appium/execute_mobile/start_service', 'POST',
    {intent, foreground, user}
  ));
}

/**
 * Stops the given service intent.
 *
 * @this {import('../driver').EspressoDriver}
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-stopservice
 * @param {string} intent - The name of the service intent to stop, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be stopped.
 * @param {string | number} [user]
 * @returns {Promise<string>} `true` if the service has been successfully stopped
 * @throws {Error} If there was a failure while stopping the service
 * or required options are missing
 */
export async function mobileStopService (intent, user) {
  return /** @type {string} */ (await this.espresso.jwproxy.command(
    '/appium/execute_mobile/stop_service', 'POST', {intent, user}
  ));
}
