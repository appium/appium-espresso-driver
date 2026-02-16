import path from 'node:path';
import _ from 'lodash';
import {node} from 'appium/support';
// Using broad typing to keep test helper concise
type W3CCapabilities = any;

const APIDEMOS_APK_URL =
  'https://github.com/appium/android-apidemos/releases/download/v6.0.2/ApiDemos-debug.apk';

export function amendCapabilities(
  baseCaps: W3CCapabilities,
  ...newCaps: Array<Record<string, any>>
) {
  return node.deepFreeze({
    alwaysMatch: _.cloneDeep(Object.assign({}, baseCaps.alwaysMatch, ...newCaps)),
    firstMatch: [{}],
  }) as W3CCapabilities;
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

export const COMPOSE_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': path.resolve(__dirname, '..', 'assets', 'compose_playground.apk'),
  'appium:espressoBuildConfig':
    '{"additionalAndroidTestDependencies": ' +
    '["androidx.lifecycle:lifecycle-extensions:2.2.0", ' +
    '"androidx.activity:activity:1.3.1", ' +
    '"androidx.fragment:fragment:1.3.4"]}',
});
