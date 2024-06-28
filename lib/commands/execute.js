import {AndroidDriver} from 'appium-android-driver';

/**
 * @this {EspressoDriver}
 * @returns {import('@appium/types').StringRecord<string>}
 */
export function mobileCommandsMapping() {
  const commonMapping = new AndroidDriver().mobileCommandsMapping.call(this);
  return {
    ...commonMapping,
    swipe: 'mobileSwipe',
    scrollToPage: 'mobileScrollToPage',
    navigateTo: 'mobileNavigateTo',
    clickAction: 'mobileClickAction',

    deviceInfo: 'mobileGetDeviceInfo',

    isToastVisible: 'mobileIsToastVisible',

    openDrawer: 'mobileOpenDrawer',
    closeDrawer: 'mobileCloseDrawer',

    setDate: 'mobileSetDate',
    setTime: 'mobileSetTime',

    backdoor: 'mobileBackdoor',

    flashElement: 'mobileFlashElement',

    uiautomator: 'mobileUiautomator',
    uiautomatorPageSource: 'mobileUiautomatorPageSource',

    webAtoms: 'mobileWebAtoms',

    dismissAutofill: 'mobileDismissAutofill',

    registerIdlingResources: 'mobileRegisterIdlingResources',
    unregisterIdlingResources: 'mobileUnregisterIdlingResources',
    listIdlingResources: 'mobileListIdlingResources',
    waitForUIThread: 'mobileWaitForUIThread',

    pressKey: 'mobilePressKey',

    setClipboard: 'mobileSetClipboard',
    getClipboard: 'mobileGetClipboard',
  };
}

/**
 * @typedef {import('../driver').EspressoDriver} EspressoDriver
 */
