import _ from 'lodash';
import { fs, tempDir } from 'appium-support';
import log from '../logger';

let commands = {}, helpers = {}, extensions = {};


commands.getStrings = async function (language) {
  if (!language) {
    language = await this.adb.getDeviceLanguage();
    log.info(`No language specified, returning strings for: ${language}`);
  }

  // Clients require the resulting mapping to have both keys
  // and values of type string
  const preprocessStringsMap = function (mapping) {
    const result = {};
    for (const [key, value] of _.toPairs(mapping)) {
      result[key] = _.isString(value) ? value : JSON.stringify(value);
    }
    return result;
  };

  if (this.apkStrings[language]) {
    // Return cached strings
    return preprocessStringsMap(this.apkStrings[language]);
  }

  if (!this.opts.app && !this.opts.appPackage) {
    log.errorAndThrow("One of 'app' or 'appPackage' capabilities should must be specified");
  }

  let app = this.opts.app;
  const tmpRoot = await tempDir.openDir();
  try {
    if (!app) {
      try {
        app = await this.adb.pullApk(this.opts.appPackage, tmpRoot);
      } catch (err) {
        log.errorAndThrow(`Failed to pull an apk from '${this.opts.appPackage}'. Original error: ${err.message}`);
      }
    }

    if (!await fs.exists(app)) {
      log.errorAndThrow(`The app at '${app}' does not exist`);
    }

    try {
      const {apkStrings} = await this.adb.extractStringsFromApk(app, language, tmpRoot);
      this.apkStrings[language] = apkStrings;
      return preprocessStringsMap(apkStrings);
    } catch (err) {
      log.errorAndThrow(`Cannot extract strings from '${app}'. Original error: ${err.message}`);
    }
  } finally {
    await fs.rimraf(tmpRoot);
  }
};

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
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${element}/swipe`, 'POST', {direction});
};

commands.mobileGetDeviceInfo = async function () {
  return await this.espresso.jwproxy.command('/appium/device/info', 'GET');
};

Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
