import { requireOptions } from '../utils';

/**
 * Stop proxying to any Chromedriver and redirect to Espresso
 *
 * @this {import('../driver').EspressoDriver}
 * @returns {void}
 */
export function suspendChromedriverProxy () {
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
 * @this {import('../driver').EspressoDriver}
 */
export async function mobileWebAtoms (opts = {}) {
  opts = requireOptions(opts, ['methodChain']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/web_atoms`, 'POST', opts);
}
