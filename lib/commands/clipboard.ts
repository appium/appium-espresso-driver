import type {EspressoDriver} from '../driver';

/**
 * Gets the clipboard content from the device.
 * @returns Promise that resolves to base64-encoded content of the clipboard
 * or an empty string if the clipboard is empty.
 */
export async function getClipboard(this: EspressoDriver): Promise<string> {
  return (await this.adb.getApiLevel()) < 29
    ? ((await this.espresso.jwproxy.command('/appium/device/get_clipboard', 'POST', {})) as string)
    : await this.settingsApp.getClipboard();
}

/**
 * Sets the clipboard content on the device.
 * @param content - Base64-encoded clipboard payload
 * @param contentType - Only a single content type is supported, which is 'plaintext'
 * @param label - Optional label to identify the current clipboard payload
 * @returns Promise that resolves when the clipboard is set
 */
export async function mobileSetClipboard(
  this: EspressoDriver,
  content: string,
  contentType?: 'plaintext',
  label?: string,
): Promise<void> {
  await this.espresso.jwproxy.command('/appium/device/set_clipboard', 'POST', {
    content,
    contentType,
    label,
  });
}
