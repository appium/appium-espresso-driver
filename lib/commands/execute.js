import _ from 'lodash';
import { errors } from 'appium/driver';

const extensions = {};

/**
 * @this {import('../driver').EspressoDriver}
 * @param {string} mobileCommand
 * @param {Record<string, any>} [opts={}]
 * @returns {Promise<any>}
 */
extensions.executeMobile = async function executeMobile (mobileCommand, opts = {}) {
  const mobileCommandsMapping = {
    shell: 'mobileShell',

    execEmuConsoleCommand: 'mobileExecEmuConsoleCommand',

    performEditorAction: 'mobilePerformEditorAction',

    changePermissions: 'mobileChangePermissions',
    getPermissions: 'mobileGetPermissions',

    startScreenStreaming: 'mobileStartScreenStreaming',
    stopScreenStreaming: 'mobileStopScreenStreaming',

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

    getDeviceTime: 'mobileGetDeviceTime',

    backdoor: 'mobileBackdoor',

    flashElement: 'mobileFlashElement',

    uiautomator: 'mobileUiautomator',
    uiautomatorPageSource: 'mobileUiautomatorPageSource',

    webAtoms: 'mobileWebAtoms',
    getContexts: 'mobileGetContexts',

    dismissAutofill: 'mobileDismissAutofill',

    getNotifications: 'mobileGetNotifications',

    listSms: 'mobileListSms',

    pushFile: 'mobilePushFile',
    pullFile: 'mobilePullFile',
    pullFolder: 'mobilePullFolder',
    deleteFile: 'mobileDeleteFile',

    isAppInstalled: 'mobileIsAppInstalled',
    queryAppState: 'mobileQueryAppState',
    activateApp: 'mobileActivateApp',
    removeApp: 'mobileRemoveApp',
    terminateApp: 'mobileTerminateApp',
    installApp: 'mobileInstallApp',
    clearApp: 'mobileClearApp',
    backgroundApp: 'mobileBackgroundApp',
    getCurrentActivity: 'getCurrentActivity',
    getCurrentPackage: 'getCurrentPackage',

    startActivity: 'mobileStartActivity',
    startService: 'mobileStartService',
    stopService: 'mobileStopService',
    broadcast: 'mobileBroadcast',

    registerIdlingResources: 'mobileRegisterIdlingResources',
    unregisterIdlingResources: 'mobileUnregisterIdlingResources',
    listIdlingResources: 'mobileListIdlingResources',
    waitForUIThread: 'mobileWaitForUIThread',

    lock: 'mobileLock',
    unlock: 'mobileUnlock',
    isLocked: 'isLocked',

    refreshGpsCache: 'mobileRefreshGpsCache',

    startMediaProjectionRecording: 'mobileStartMediaProjectionRecording',
    isMediaProjectionRecordingRunning: 'mobileIsMediaProjectionRecordingRunning',
    stopMediaProjectionRecording: 'mobileStopMediaProjectionRecording',

    getConnectivity: 'mobileGetConnectivity',
    setConnectivity: 'mobileSetConnectivity',
    toggleGps: 'toggleLocationServices',
    isGpsEnables: 'isLocationServicesEnabled',

    pressKey: 'mobilePressKey',
    hideKeyboard: 'hideKeyboard',
    isKeyboardShown: 'isKeyboardShown',

    getDisplayDensity: 'getDisplayDensity',
    getSystemBars: 'getSystemBars',
    fingerprint: 'mobileFingerprint',

    sendSms: 'mobileSendSms',
    gsmCall: 'mobileGsmCall',
    gsmSignal: 'mobileGsmSignal',
    gsmVoice: 'mobileGsmVoice',
    powerAc: 'mobilePowerAC',
    powerCapacity: 'mobilePowerCapacity',
    networkSpeed: 'mobileNetworkSpeed',
    sensorSet: 'sensorSet',

    getPerformanceData: 'mobileGetPerformanceData',
    getPerformanceDataTypes: 'getPerformanceDataTypes',

    statusBar: 'mobilePerformStatusBarCommand',

    screenshots: 'mobileScreenshots',

    setUiMode: 'mobileSetUiMode',
    getUiMode: 'mobileGetUiMode',
  };

  if (!_.has(mobileCommandsMapping, mobileCommand)) {
    throw new errors.UnknownCommandError(`Unknown mobile command "${mobileCommand}". ` +
      `Only ${_.keys(mobileCommandsMapping)} commands are supported.`);
  }
  return await this[mobileCommandsMapping[mobileCommand]](opts);
};

export default extensions;
