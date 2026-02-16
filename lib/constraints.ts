import {Constraints} from '@appium/types';
import {commonCapConstraints} from 'appium-android-driver';

export const ESPRESSO_CONSTRAINTS = {
  systemPort: {
    isNumber: true,
  },
  launchTimeout: {
    isNumber: true,
  },
  forceEspressoRebuild: {
    isBoolean: true,
  },
  espressoServerLaunchTimeout: {
    isNumber: true,
  },
  espressoBuildConfig: {
    isString: true,
  },
  showGradleLog: {
    isBoolean: true,
  },
  skipServerInstallation: {
    isBoolean: true,
  },
  intentOptions: {
    isObject: true,
  },
  disableSuppressAccessibilityService: {
    isBoolean: true,
  },
  activityOptions: {
    isObject: true,
  },
  appLocale: {
    isObject: true,
  },
  ...commonCapConstraints,
} as const satisfies Constraints;

export type EspressoConstraints = typeof ESPRESSO_CONSTRAINTS;
