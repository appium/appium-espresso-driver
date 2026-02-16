import type {EspressoDriver} from '../driver';
import type {StringRecord} from '@appium/types';

/**
 * Stop proxying to any Chromedriver and redirect to Espresso
 *
 * @returns void
 */
export function suspendChromedriverProxy(this: EspressoDriver): void {
  this.chromedriver = undefined;
  this.proxyReqRes = this.espresso.proxyReqRes.bind(this.espresso);
  this.proxyCommand = this.espresso.proxyCommand.bind(this.espresso);
  this.jwpProxyActive = true;
}

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
 * @see https://github.com/appium/appium-espresso-driver?tab=readme-ov-file#mobile-webatoms
 * @param webviewEl - Optional webview element ID to operate on
 * @param forceJavascriptEnabled - If webview disables javascript, webatoms won't work, this forces it
 * @param methodChain - Array of method chain objects, each containing a name and atom with name and args
 * @returns Promise that resolves to the result of executing the web atoms
 */
export async function mobileWebAtoms(
  this: EspressoDriver,
  webviewEl?: string,
  forceJavascriptEnabled?: boolean,
  methodChain?: StringRecord[],
): Promise<any> {
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/web_atoms`, 'POST', {
    webviewEl,
    forceJavascriptEnabled,
    methodChain,
  });
}
