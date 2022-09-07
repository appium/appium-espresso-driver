import _ from 'lodash';
import { errors } from 'appium/driver';

const extensions = {};

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

    sensorSet: 'sensorSet',

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

    startActivity: 'mobileStartActivity',
    startService: 'mobileStartService',
    stopService: 'mobileStopService',
    broadcast: 'mobileBroadcast',

    registerIdlingResources: 'mobileRegisterIdlingResources',
    unregisterIdlingResources: 'mobileUnregisterIdlingResources',
    listIdlingResources: 'mobileListIdlingResources',

    unlock: 'mobileUnlock',

    refreshGpsCache: 'mobileRefreshGpsCache',

    startMediaProjectionRecording: 'mobileStartMediaProjectionRecording',
    isMediaProjectionRecordingRunning: 'mobileIsMediaProjectionRecordingRunning',
    stopMediaProjectionRecording: 'mobileStopMediaProjectionRecording',
  };

  if (!_.has(mobileCommandsMapping, mobileCommand)) {
    throw new errors.UnknownCommandError(`Unknown mobile command "${mobileCommand}". ` +
      `Only ${_.keys(mobileCommandsMapping)} commands are supported.`);
  }
  return await this[mobileCommandsMapping[mobileCommand]](opts);
};

export default extensions;
