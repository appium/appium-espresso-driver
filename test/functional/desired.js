import path from 'path';
import _ from 'lodash';
import gpsdemoApp from 'gps-demo-app';

const apidemosApp = require.resolve('android-apidemos');

function deepFreeze (object) {
  const propNames = Object.getOwnPropertyNames(object);
  for (const name of propNames) {
    const value = object[name];
    if (value && typeof value === 'object') {
      deepFreeze(value);
    }
  }
  return Object.freeze(object);
}

function amendCapabilities (baseCaps, ...newCaps) {
  return deepFreeze({
    alwaysMatch: _.cloneDeep(Object.assign({}, baseCaps.alwaysMatch, ...newCaps)),
    firstMatch: [{}],
  });
}

const GENERIC_CAPS = deepFreeze({
  alwaysMatch: {
    'appium:androidInstallTimeout': process.env.CI ? 120000 : 90000,
    'appium:deviceName': 'Android',
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

const GPS_CAPS = amendCapabilities(GENERIC_CAPS, {
  'appium:app': gpsdemoApp,
});

export {
  GENERIC_CAPS, APIDEMO_CAPS, GPS_CAPS, COMPOSE_CAPS,
  amendCapabilities,
};
