import type {Capabilities} from '@wdio/types';
import {node} from 'appium/support.js';
import {getComposePlaygroundPath} from '../setup.js';

export type ComposeCaps = Capabilities.W3CCapabilities;

const APIDEMOS_APK_URL =
  'https://github.com/appium/android-apidemos/releases/download/v6.0.2/ApiDemos-debug.apk';

export function amendCapabilities(baseCaps: ComposeCaps, ...newCaps: Array<Record<string, any>>) {
  return node.deepFreeze({
    alwaysMatch: structuredClone(Object.assign({}, baseCaps.alwaysMatch, ...newCaps)),
    firstMatch: [{}],
  }) as ComposeCaps;
}

export const GENERIC_CAPS = node.deepFreeze({
  alwaysMatch: {
    'appium:androidInstallTimeout': process.env.CI ? 120000 : 90000,
    'appium:deviceName': 'Android',
    'appium:automationName': 'Espresso',
    platformName: 'Android',
    'appium:forceEspressoRebuild': true,
    'appium:adbExecTimeout': process.env.CI ? 120000 : 20000,
    'appium:espressoServerLaunchTimeout': process.env.CI ? 120000 : 30000,
    'appium:printPageSourceOnFindFailure': true,
  },
  firstMatch: [{}],
});

export const APIDEMO_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': APIDEMOS_APK_URL,
});

export async function getComposeCaps(): Promise<ComposeCaps> {
  const composeApp = await getComposePlaygroundPath();
  return amendCapabilities(GENERIC_CAPS, {
    'appium:app': composeApp,
  });
}
