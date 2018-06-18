import gpsdemoApp from 'gps-demo-app';
const apidemosApp = require.resolve('android-apidemos');


const GENERIC_CAPS = {
  androidInstallTimeout: 90000,
  deviceName: 'Android',
  platformName: 'Android',
  forceEspressoRebuild: true,
  adbExecTimeout: process.env.CI ? 120000 : 20000,
  espressoServerLaunchTimeout: process.env.CI ? 120000 : 30000,
};

const APIDEMO_CAPS = Object.assign({}, GENERIC_CAPS, {
  app: apidemosApp,
});

const GPS_CAPS = Object.assign({}, GENERIC_CAPS, {
  app: gpsdemoApp,
});

export { GENERIC_CAPS, APIDEMO_CAPS, GPS_CAPS };
