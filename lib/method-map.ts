import {AndroidDriver} from 'appium-android-driver';
import type {MethodMap} from '@appium/types';

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
