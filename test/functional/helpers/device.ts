import assert from 'node:assert/strict';

import type {Browser} from 'webdriverio';

import {isApiBelow} from './env.js';

const APIDEMOS_PACKAGE = 'io.appium.android.apis';

/**
 * Brings ApiDemos back to its root screen without pressing Back,
 * which finishes the app (and kills the session) when it is already at the root.
 */
export async function goToHome(driver: Browser): Promise<void> {
  // 'mobile: startActivity' hangs when the target activity is already on top
  await driver.execute('mobile: shell', {
    command: 'am',
    args: ['start', '-n', `${APIDEMOS_PACKAGE}/.ApiDemos`, '--activity-clear-top'],
  });
  await driver.waitUntil(async () => (await driver.getCurrentActivity()).endsWith('.ApiDemos'), {
    timeout: 15000,
    timeoutMsg: 'ApiDemos root activity did not come to the foreground',
  });
}

/** Fails loudly on gesture navigation, where edge swipes open the app history or go Back. */
export async function assertThreeButtonNavigation(driver: Browser): Promise<void> {
  if (isApiBelow(29)) {
    return; // gesture navigation does not exist
  }
  const mode = String(
    await driver.execute('mobile: shell', {command: 'settings', args: ['get', 'secure', 'navigation_mode']}),
  ).trim();
  assert.ok(
    mode === '0' || mode === 'null',
    `Emulator must use 3-button navigation (navigation_mode=${mode}). ` +
      'Run: adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton',
  );
}
