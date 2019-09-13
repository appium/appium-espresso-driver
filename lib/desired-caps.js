import { commonCapConstraints } from 'appium-android-driver';

let espressoCapConstraints = {
  app: {
    presence: true,
    isString: true,
  },
  automationName: {
    isString: true,
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
  }
};

let desiredCapConstraints = {};
Object.assign(desiredCapConstraints, espressoCapConstraints,
              commonCapConstraints);

export default desiredCapConstraints;
