import type {EspressoDriver} from '../driver';

/**
 * Starts the given service intent.
 *
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-startservice
 * @param intent - The name of the service intent to start, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be started.
 * @param foreground - Set it to `true` if your service must be
 * started as foreground service. Defaults to `false`.
 * @param user - Optional user identifier (string or number)
 * @returns Promise that resolves to the full component name
 * @throws {Error} If there was a failure while starting the service
 * or required options are missing
 */
export async function mobileStartService(
  this: EspressoDriver,
  intent: string,
  foreground?: boolean,
  user?: string | number,
): Promise<string> {
  return (await this.espresso.jwproxy.command('/appium/execute_mobile/start_service', 'POST', {
    intent,
    foreground,
    user,
  })) as string;
}

/**
 * Stops the given service intent.
 *
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-stopservice
 * @param intent - The name of the service intent to stop, for example
 * `com.some.package.name/.YourServiceSubClassName`. This option is mandatory.
 * !!! Only services in the app's under test scope could be stopped.
 * @param user - Optional user identifier (string or number)
 * @returns Promise that resolves to `true` if the service has been successfully stopped
 * @throws {Error} If there was a failure while stopping the service
 * or required options are missing
 */
export async function mobileStopService(
  this: EspressoDriver,
  intent: string,
  user?: string | number,
): Promise<string> {
  return (await this.espresso.jwproxy.command('/appium/execute_mobile/stop_service', 'POST', {
    intent,
    user,
  })) as string;
}
