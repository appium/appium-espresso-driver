import _ from 'lodash';
import { util } from 'appium-support';
import logger from '../logger';

let commands = {}, helpers = {}, extensions = {};

function assertRequiredOptions (options, requiredOptionNames) {
  if (!_.isArray(requiredOptionNames)) {
    requiredOptionNames = [requiredOptionNames];
  }
  const presentOptionNames = _.keys(options);
  const missingOptionNames = _.difference(requiredOptionNames, presentOptionNames);
  if (_.isEmpty(missingOptionNames)) {
    return options;
  }
  throw new Error(`The following options are required: ${JSON.stringify(missingOptionNames)}. ` +
    `You have only provided: ${JSON.stringify(presentOptionNames)}`);
}

commands.mobilePerformEditorAction = async function (opts = {}) {
  const {action} = assertRequiredOptions(opts, ['action']);
  return await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
};

commands.mobileSwipe = async function (opts = {}) {
  const {direction, element} = assertRequiredOptions(opts, ['direction', 'element']);
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/swipe`, 'POST', {direction});
};

commands.mobileGetDeviceInfo = async function () {
  return await this.espresso.jwproxy.command('/appium/device/info', 'GET');
};

commands.mobileIsToastVisible = async function (opts = {}) {
  const {text, isRegexp} = opts;
  if (!util.hasValue(text)) {
    logger.errorAndThrow(`'text' argument is mandatory`);
  }
  return await this.espresso.jwproxy.command('/appium/execute_mobile/is_toast_displayed', 'POST', {
    text,
    isRegexp,
  });
};

commands.mobileOpenDrawer = async function (opts = {}) {
  const {element, gravity} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/open_drawer`, 'POST', {
    gravity
  });
};

commands.mobileCloseDrawer = async function (opts = {}) {
  const {element, gravity} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/close_drawer`, 'POST', {
    gravity
  });
};

commands.mobileSetDate = async function (opts = {}) {
  const {element, year, monthOfYear, dayOfMonth} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_date`, 'POST', {
    year,
    monthOfYear,
    dayOfMonth,
  });
};

commands.mobileSetTime = async function (opts = {}) {
  const {element, hours, minutes} = assertRequiredOptions(opts, ['element']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_time`, 'POST', {
    hours,
    minutes,
  });
};

commands.mobileNavigateTo = async function (opts = {}) {
  let {element, menuItemId} = assertRequiredOptions(opts, ['menuItemId', 'element']);

  let menuItemIdAsNumber = parseInt(menuItemId, 0);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    logger.errorAndThrow(`'menuItemId' must be a non-negative number. Found ${menuItemId}`);
    menuItemId = menuItemIdAsNumber;
  }

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/navigate_to`, 'POST', {
    menuItemId
  });
};

commands.mobileScrollToPage = async function (opts = {}) {
  let {element, scrollTo, scrollToPage, smoothScroll} = assertRequiredOptions(opts, ['element']);

  // Validate that one of scrollTo or scrollToPage was set
  if (!util.hasValue(scrollTo) && !util.hasValue(scrollToPage)) {
    logger.errorAndThrow(`Must set either 'scrollTo' or 'scrollToPage' parameter`);
  }

  // Validate that only one of scrollTo or scrollToPage was set
  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    logger.errorAndThrow(`'scrollTo' and 'scrollToPage' are mutually exclusive parameters and cannot be set at the same time`);
  }

  // Validate that scrollTo is an accepted value
  const scrollToOpts = ['first', 'last', 'left', 'right'];
  if (util.hasValue(scrollTo)) {
    if (!_.isString(scrollTo) || !scrollToOpts.includes(scrollTo.toLowerCase())) {
      logger.errorAndThrow(`'scrollTo' options must be one of ${scrollToOpts}`);
    }
    scrollTo = scrollTo.toLowerCase();
  }

  // Validate that scrollToPage is a non-negative integer
  if (util.hasValue(scrollToPage)) {
    let scrollToPageAsNumber = parseInt(scrollToPage, 0);
    if (_.isNaN(scrollToPageAsNumber) || scrollToPage < 0) {
      logger.errorAndThrow(`'scrollToPage' must be a non-negative integer. Found ${scrollToPage}`);
    }
    scrollToPage = scrollToPageAsNumber;
  }

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/scroll_to_page`, 'POST', {
    scrollTo,
    scrollToPage,
    smoothScroll,
  });
};

// Stop proxying to any Chromedriver and redirect to Espresso
helpers.suspendChromedriverProxy = function () {
  this.chromedriver = null;
  this.proxyReqRes = this.espresso.proxyReqRes.bind(this.espresso);
  this.jwpProxyActive = true;
};


Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
