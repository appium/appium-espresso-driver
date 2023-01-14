import path from 'path';
import _ from 'lodash';
import { node } from 'appium/support';
import { API_DEMOS_APK_PATH as apidemosApp } from 'android-apidemos';

function amendCapabilities (baseCaps, ...newCaps) {
  return node.deepFreeze({
    alwaysMatch: _.cloneDeep(Object.assign({}, baseCaps.alwaysMatch, ...newCaps)),
    firstMatch: [{}],
  });
}

const GENERIC_CAPS = node.deepFreeze({
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
  firstMatch: [{}]
});

const APIDEMO_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': apidemosApp,
});

const COMPOSE_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': path.resolve('test', 'assets', 'compose_playground.apk'),
  'appium:espressoBuildConfig': '{"additionalAndroidTestDependencies": ' +
    '["androidx.lifecycle:lifecycle-extensions:2.2.0", ' +
    '"androidx.activity:activity:1.3.1", ' +
    '"androidx.fragment:fragment:1.3.4"]}'
});

// http://www.impressive-artworx.de/tutorials/android/gps_tutorial_1.zip
const gpsdemoApp = path.resolve(__dirname, '..', 'assets', 'gpsDemo-debug.apk');

const GPS_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': gpsdemoApp,
});

export {
  GENERIC_CAPS, APIDEMO_CAPS, GPS_CAPS, COMPOSE_CAPS,
  amendCapabilities,
};
