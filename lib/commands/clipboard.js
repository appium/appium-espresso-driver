/**
 * @this {EspressoDriver}
 * @returns {Promise<string>} Base64-encoded content of the clipboard
 * or an empty string if the clipboard is empty.
 */
export async function getClipboard () {
  return /** @type {string} */ ((await this.adb.getApiLevel() < 29)
    ? (await this.espresso.jwproxy.command('/appium/device/get_clipboard', 'POST', {}))
    : (await this.settingsApp.getClipboard()));
}

/**
 * @this {EspressoDriver}
 * @param {string} content Base64-encoded clipboard payload
 * @param {'plaintext'} [contentType] Only a single
 * content type is supported, which is 'plaintext'
 * @param {string} [label] Optinal label to identify the current
 * clipboard payload
 * @returns {Promise<void>}
 */
export async function mobileSetClipboard(content, contentType, label) {
  await this.espresso.jwproxy.command(
    '/appium/device/set_clipboard',
    'POST',
    {content, contentType, label}
  );
}

/**
 * @typedef {import('../driver').EspressoDriver} EspressoDriver
 */
