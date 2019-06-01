import _ from 'lodash';
import { errors } from 'appium-base-driver';

let extensions = {};

extensions.executeMobile = async function executeMobile (mobileCommand, opts = {}) {
  const mobileCommandsMapping = {
    shell: 'mobileShell',

    performEditorAction: 'mobilePerformEditorAction',

    changePermissions: 'mobileChangePermissions',
    getPermissions: 'mobileGetPermissions',

    swipe: 'mobileSwipe',

    deviceInfo: 'mobileGetDeviceInfo',

    isToastVisible: 'mobileIsToastVisible',

    openDrawer: 'mobileOpenDrawer',

    closeDrawer: 'mobileCloseDrawer',

    setDate: 'mobileSetDate',

    setTime: 'mobileSetTime',

    navigateTo: 'mobileNavigateTo',

    scrollToPage: 'mobileScrollToPage',

    backdoor: 'mobileBackdoor',

    flashElement: 'mobileFlashElement',

    uiautomator: 'mobileUiautomator',

    clickAction: 'mobileClickAction',

    webAtoms: 'mobileWebAtoms',

    dismissAutofill: 'mobileDismissAutofill',
  };

  if (!_.has(mobileCommandsMapping, mobileCommand)) {
    throw new errors.UnknownCommandError(`Unknown mobile command "${mobileCommand}". ` +
      `Only ${_.keys(mobileCommandsMapping)} commands are supported.`);
  }
  return await this[mobileCommandsMapping[mobileCommand]](opts);
};

export default extensions;
