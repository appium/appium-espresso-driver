import { commonCapConstraints } from 'appium-android-driver';

const espressoCapConstraints = {
  systemPort: {
    isNumber: true
  },
  launchTimeout: {
    isNumber: true
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
  },
};

export const desiredCapConstraints = {
  ...commonCapConstraints,
  ...espressoCapConstraints,
};
