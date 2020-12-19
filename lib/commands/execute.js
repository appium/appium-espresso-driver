import _ from 'lodash';
import { errors } from 'appium-base-driver';

let extensions = {};

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

    deleteFile: 'mobileDeleteFile',

    startService: 'mobileStartService',
    stopService: 'mobileStopService',

    registerIdlingResources: 'mobileRegisterIdlingResources',
    unregisterIdlingResources: 'mobileUnregisterIdlingResources',
    listIdlingResources: 'mobileListIdlingResources',
  };

  if (!_.has(mobileCommandsMapping, mobileCommand)) {
    throw new errors.UnknownCommandError(`Unknown mobile command "${mobileCommand}". ` +
      `Only ${_.keys(mobileCommandsMapping)} commands are supported.`);
  }
  return await this[mobileCommandsMapping[mobileCommand]](opts);
};

export default extensions;
