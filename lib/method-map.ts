import type {MethodMap} from '@appium/types';
import {AndroidDriver} from 'appium-android-driver';

export const newMethodMap = {
  ...AndroidDriver.newMethodMap,
  '/session/:sessionId/appium/device/get_clipboard': {
    POST: {
      command: 'getClipboard',
      payloadParams: {optional: ['contentType']},
      deprecated: true,
    },
  },
} as const satisfies MethodMap<any>;
