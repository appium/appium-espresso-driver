import _ from 'lodash';
import { util } from 'appium-support';
import logger from '../logger';
import validate from 'validate.js';

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
  const {element, year, monthOfYear, dayOfMonth} = assertRequiredOptions(opts, ['element', 'year', 'monthOfYear', 'dayOfMonth']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_date`, 'POST', {
    year,
    monthOfYear,
    dayOfMonth,
  });
};

commands.mobileSetTime = async function (opts = {}) {
  const {element, hours, minutes} = assertRequiredOptions(opts, ['element', 'hours', 'minutes']);

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/set_time`, 'POST', {
    hours,
    minutes,
  });
};

commands.mobileNavigateTo = async function (opts = {}) {
  let {element, menuItemId} = assertRequiredOptions(opts, ['menuItemId', 'element']);

  let menuItemIdAsNumber = parseInt(menuItemId, 10);
  if (_.isNaN(menuItemIdAsNumber) || menuItemIdAsNumber < 0) {
    logger.errorAndThrow(`'menuItemId' must be a non-negative number. Found ${menuItemId}`);
    menuItemId = menuItemIdAsNumber;
  }

  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${util.unwrapElement(element)}/navigate_to`, 'POST', {
    menuItemId
  });
};

commands.mobileScrollToPage = async function (opts = {}) {

  // Validate the parameters
  const scrollToTypes = ['first', 'last', 'left', 'right'];
  const res = validate(opts, {
    element: {presence: true},
    scrollTo: {
      inclusion: {
        within: scrollToTypes,
        message: `"scrollTo" must be one of "${scrollToTypes.join(", ")}" found '%{value}'`,
      }
    },
    scrollToPage: {
      numericality: {
        onlyInteger: true,
        greaterThanOrEqualTo: 0,
        message: `"scrollToPage" must be a non-negative integer. Found '%{value}'`
      },
    },
  });

  if (util.hasValue(res)) {
    logger.errorAndThrow(`Invalid scrollTo parameters: ${JSON.stringify(res)}`);
  }

  const {element, scrollTo, scrollToPage, smoothScroll} = opts;

  if (util.hasValue(scrollTo) && util.hasValue(scrollToPage)) {
    logger.warn(`'scrollTo' and 'scrollToPage' where both provided. Defaulting to 'scrollTo'`);
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
