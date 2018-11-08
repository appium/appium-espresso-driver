import _ from 'lodash';
import { util } from 'appium-support';

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

// Stop proxying to any Chromedriver and redirect to Espresso
helpers.suspendChromedriverProxy = function () {
  this.chromedriver = null;
  this.proxyReqRes = this.espresso.proxyReqRes.bind(this.espresso);
  this.jwpProxyActive = true;
};


Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
