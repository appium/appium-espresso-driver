import { commonCapConstraints } from 'appium-android-driver';

let espressoCapConstraints = {
  app: {
    isString: true,
  },
  automationName: {
    isString: true,
  },
  systemPort: {
    isNumber: true
  },
  browserName: {
    isString: true
  },
  launchTimeout: {
    isNumber: true
  },
  skipUnlock: {
    isBoolean: true
  },
  forceEspressoRebuild: {
    isBoolean: true
  },
  espressoServerLaunchTimeout: {
    isNumber: true
  },
  espressoBuildConfig: {
    isString: true
  },
  showGradleLog: {
    isBoolean: true
  },
  skipServerInstallation: {
    isBoolean: true
  },
  intentOptions: {
    isObject: true
  },
  disableSuppressAccessibilityService: {
    isBoolean: true
  },
  activityOptions: {
    isObject: true
  },
  appLocale: {
    isObject: true,
  }
};

let desiredCapConstraints = {};
Object.assign(desiredCapConstraints, espressoCapConstraints,
              commonCapConstraints);

export default desiredCapConstraints;
