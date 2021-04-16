import { JWProxy, errors } from 'appium-base-driver';
import { waitForCondition } from 'asyncbox';
import logger from './logger';
import { ServerBuilder, buildServerSigningConfig } from './server-builder';
import path from 'path';
import { fs, util, mkdirp, timing } from 'appium-support';
import { version } from '../../package.json'; // eslint-disable-line import/no-unresolved
import B from 'bluebird';
import _ from 'lodash';
import { copyGradleProjectRecursively } from './utils';
import axios from 'axios';


const TEST_SERVER_ROOT = path.resolve(__dirname, '..', '..', 'espresso-server');
const TEST_APK_PKG = 'io.appium.espressoserver.test';
const REQUIRED_PARAMS = [
  'adb',
  'tmpDir',
  'host',
  'systemPort',
  'devicePort',
  'appPackage',
  'forceEspressoRebuild',
];
const ESPRESSO_SERVER_LAUNCH_TIMEOUT_MS = 45000;
const TARGET_PACKAGE_CONTAINER = '/data/local/tmp/espresso.apppackage';

class EspressoProxy extends JWProxy {
  async proxyCommand (url, method, body = null) {
    const {crashed, exited} = this.instrumentationState;
    if (exited) {
      throw new errors.InvalidContextError(`'${method} ${url}' cannot be proxied to Espresso server because ` +
        `the instrumentation process has ${crashed ? 'crashed' : 'been unexpectedly terminated'}. ` +
        `Check the Appium server log and the logcat output for more details`);
    }
    return await super.proxyCommand(url, method, body);
  }
}

class EspressoRunner {
  constructor (opts = {}) {
    for (let req of REQUIRED_PARAMS) {
      if (!opts || !util.hasValue(opts[req])) {
        throw new Error(`Option '${req}' is required!`);
      }
      this[req] = opts[req];
    }
    this.jwproxy = new EspressoProxy({
      server: this.host,
      port: this.systemPort,
      base: '',
      keepAlive: true,
    });
    this.proxyReqRes = this.jwproxy.proxyReqRes.bind(this.jwproxy);
    this.jwproxy.instrumentationState = {
      exited: false,
      crashed: false,
    };

    const modServerName = fs.sanitizeName(`${TEST_APK_PKG}_${version}_${this.appPackage}_${this.adb.curDeviceId}.apk`, {
      replacement: '-',
    });
    this.modServerPath = path.resolve(this.tmpDir, modServerName);
    this.showGradleLog = opts.showGradleLog;
    this.espressoBuildConfig = opts.espressoBuildConfig;

    this.serverLaunchTimeout = opts.serverLaunchTimeout || ESPRESSO_SERVER_LAUNCH_TIMEOUT_MS;
    this.androidInstallTimeout = opts.androidInstallTimeout;

    this.disableSuppressAccessibilityService = opts.disableSuppressAccessibilityService;

    // Espresso Server app needs to be signed with same keyStore as appPackage
    if (opts.useKeystore && opts.keystorePath && opts.keystorePassword && opts.keyAlias && opts.keyPassword) {
      this.signingConfig = buildServerSigningConfig({
        keystoreFile: opts.keystorePath,
        keystorePassword: opts.keystorePassword,
        keyAlias: opts.keyAlias,
        keyPassword: opts.keyPassword
      });
    } else {
      this.signingConfig = null;
    }
  }

  async isAppPackageChanged () {
    if (!await this.adb.fileExists(TARGET_PACKAGE_CONTAINER)) {
      logger.debug('The previous target application package is unknown');
      return true;
    }
    const previousAppPackage = (await this.adb.shell(['cat', TARGET_PACKAGE_CONTAINER])).trim();
    logger.debug(`The previous target application package was '${previousAppPackage}'. ` +
      `The current package is '${this.appPackage}'`);
    return previousAppPackage !== this.appPackage;
  }

  /**
   * Installs Espresso server apk on to the device or emulator.
   * Each adb command uses default timeout by them.
   */
  async installServer () {
    const appState = await this.adb.getApplicationInstallState(this.modServerPath, TEST_APK_PKG);

    const shouldUninstallApp = [
      this.adb.APP_INSTALL_STATE.OLDER_VERSION_INSTALLED,
      this.adb.APP_INSTALL_STATE.NEWER_VERSION_INSTALLED
    ].includes(appState);
    const shouldInstallApp = shouldUninstallApp || [
      this.adb.APP_INSTALL_STATE.NOT_INSTALLED
    ].includes(appState);

    if (shouldUninstallApp) {
      logger.info(`Uninstalling Espresso Test Server apk from the target device (pkg: '${TEST_APK_PKG}')`);
      try {
        await this.adb.uninstallApk(TEST_APK_PKG);
      } catch (err) {
        logger.warn(`Error uninstalling '${TEST_APK_PKG}': ${err.message}`);
      }
    }

    if (shouldInstallApp) {
      logger.info(`Installing Espresso Test Server apk from the target device (path: '${this.modServerPath}')`);
      try {
        await this.adb.install(this.modServerPath, { replace: false, timeout: this.androidInstallTimeout });
        logger.info(`Installed Espresso Test Server apk '${this.modServerPath}' (pkg: '${TEST_APK_PKG}')`);
      } catch (err) {
        logger.errorAndThrow(`Cannot install '${this.modServerPath}' because of '${err.message}'`);
      }
    }
  }

  async installTestApk () {
    let rebuild = this.forceEspressoRebuild;
    if (rebuild) {
      logger.debug(`'forceEspressoRebuild' capability is enabled`);
    } else if (await this.isAppPackageChanged()) {
      logger.info(`Forcing Espresso server rebuild because of changed application package`);
      rebuild = true;
    }

    if (rebuild && await fs.exists(this.modServerPath)) {
      logger.debug(`Deleting the obsolete Espresso server package '${this.modServerPath}'`);
      await fs.unlink(this.modServerPath);
    }
    if (!(await fs.exists(this.modServerPath))) {
      await this.buildNewModServer();
    }
    const isSigned = await this.adb.checkApkCert(this.modServerPath, TEST_APK_PKG);
    if (!isSigned) {
      await this.adb.sign(this.modServerPath);
    }
    if ((rebuild || !isSigned) && await this.adb.uninstallApk(TEST_APK_PKG)) {
      logger.info('Uninstalled the obsolete Espresso server package from the device under test');
    }

    await this.installServer();
  }

  async buildNewModServer () {
    let buildConfiguration = {};
    if (this.espressoBuildConfig) {
      let buildConfigurationStr;
      if (await fs.exists(this.espressoBuildConfig)) {
        logger.info(`Loading the build configuration from '${this.espressoBuildConfig}'`);
        buildConfigurationStr = await fs.readFile(this.espressoBuildConfig, 'utf8');
      } else {
        logger.info(`Loading the build configuration from 'espressoBuildConfig' capability`);
        buildConfigurationStr = this.espressoBuildConfig;
      }
      try {
        buildConfiguration = JSON.parse(buildConfigurationStr);
      } catch (e) {
        logger.error('Cannot parse the build configuration JSON', e);
        throw e;
      }
    }
    const dirName = fs.sanitizeName(`espresso-server-${this.adb.curDeviceId}`, {
      replacement: '-',
    });
    const serverPath = path.resolve(this.tmpDir, dirName);
    logger.info(`Building espresso server in '${serverPath}'`);
    logger.debug(`The build folder root could be customized by changing the 'tmpDir' capability`);
    await fs.rimraf(serverPath);
    await mkdirp(serverPath);
    logger.debug(`Copying espresso server template from ('${TEST_SERVER_ROOT}' to '${serverPath}')`);
    await copyGradleProjectRecursively(TEST_SERVER_ROOT, serverPath);
    logger.debug('Bulding espresso server');
    await new ServerBuilder({
      serverPath, buildConfiguration,
      showGradleLog: this.showGradleLog,
      testAppPackage: this.appPackage,
      signingConfig: this.signingConfig
    }).build();
    const apkPath = path.resolve(serverPath, 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug', 'app-debug-androidTest.apk');
    logger.debug(`Copying built apk from '${apkPath}' to '${this.modServerPath}'`);
    await fs.copyFile(apkPath, this.modServerPath);
  }

  async cleanupSessionLeftovers () {
    logger.debug('Performing cleanup of automation leftovers');

    try {
      const {value} = (await axios({
        url: `http://${this.host}:${this.systemPort}/sessions`,
        timeout: 500,
      })).data;
      const activeSessionIds = value.map((sess) => sess.id);
      if (activeSessionIds.length) {
        logger.debug(`The following obsolete sessions are still running: ${JSON.stringify(activeSessionIds)}`);
        logger.debug('Cleaning up the obsolete sessions');
        await B.all(activeSessionIds.map((id) =>
          axios({
            url: `http://${this.host}:${this.systemPort}/session/${id}`,
            method: 'DELETE',
          })
        ));
        // Let all sessions to be properly terminated before continuing
        await B.delay(1000);
      } else {
        logger.debug('No obsolete sessions have been detected');
      }
    } catch (e) {
      logger.debug(`No obsolete sessions have been detected (${e.message})`);
    }
  }

  async startSession (caps) {
    await this.cleanupSessionLeftovers();

    const cmd = [
      'shell',
      'am', 'instrument',
      '-w',
      '-e', 'debug', process.env.ESPRESSO_JAVA_DEBUG === 'true',
      '-e', 'disableAnalytics', true, // To avoid unexpected error by google analytics
    ];

    if (_.isBoolean(this.disableSuppressAccessibilityService)) {
      cmd.push('-e', 'DISABLE_SUPPRESS_ACCESSIBILITY_SERVICES', this.disableSuppressAccessibilityService);
    }

    cmd.push(`${TEST_APK_PKG}/androidx.test.runner.AndroidJUnitRunner`);

    logger.info(`Starting Espresso Server v${version} with cmd: adb ${cmd.join(' ')}`);

    let hasSocketError = false;
    // start the instrumentation process
    this.jwproxy.instrumentationState = {
      exited: false,
      crashed: false,
    };
    this.instProcess = this.adb.createSubProcess(cmd);
    this.instProcess.on('exit', (code, signal) => {
      logger.info(`Instrumentation process exited with code ${code} from signal ${signal}`);
      this.jwproxy.instrumentationState.exited = true;
    });
    this.instProcess.on('output', (stdout, stderr) => {
      const line = stdout || stderr;
      if (_.isEmpty(line.trim())) {
        // Do not print empty lines into the system log
        return;
      }

      logger.debug(`[Instrumentation] ${line.trim()}`);
      // A 'SocketException' indicates that we couldn't connect to the Espresso server,
      // because the INTERNET permission is not set
      if (line.toLowerCase().includes('java.net.socketexception')) {
        hasSocketError = true;
      } else if (line.includes('Process crashed')) {
        this.jwproxy.instrumentationState.crashed = true;
      }
    });

    const timer = new timing.Timer().start();
    await this.instProcess.start(0);
    logger.info(`Waiting up to ${this.serverLaunchTimeout}ms for Espresso server to be online`);
    try {
      await waitForCondition(async () => {
        if (hasSocketError) {
          logger.errorAndThrow(`Espresso server has failed to start due to an unexpected exception. ` +
            `Make sure the 'INTERNET' permission is requested in the Android manifest of your ` +
            `application under test (<uses-permission android:name="android.permission.INTERNET" />)`);
        } else if (this.jwproxy.instrumentationState.exited) {
          logger.errorAndThrow(`Espresso server process has been unexpectedly terminated. ` +
            `Check the Appium server log and the logcat output for more details`);
        }
        try {
          await this.jwproxy.command('/status', 'GET');
          return true;
        } catch (e) {
          return false;
        }
      }, {
        waitMs: this.serverLaunchTimeout,
        intervalMs: 500,
      });
    } catch (e) {
      if (/Condition unmet/.test(e.message)) {
        logger.errorAndThrow(`Timed out waiting for Espresso server to be ` +
          `online within ${this.serverLaunchTimeout}ms. The timeout value could be ` +
          `customized using 'espressoServerLaunchTimeout' capability`);
      }
      throw e;
    }
    logger.info(`Espresso server is online. ` +
      `The initialization process took ${timer.getDuration().asMilliSeconds.toFixed(0)}ms`);
    logger.info('Starting the session');

    await this.jwproxy.command('/session', 'POST', {
      capabilities: {
        firstMatch: [caps],
        alwaysMatch: {}
      }
    });
    await this.recordTargetAppPackage();
  }

  async recordTargetAppPackage () {
    await this.adb.shell([`echo "${this.appPackage}" > "${TARGET_PACKAGE_CONTAINER}"`]);
    logger.info(`Recorded the target application package '${this.appPackage}' to ${TARGET_PACKAGE_CONTAINER}`);
  }

  async deleteSession () {
    logger.debug('Deleting Espresso server session');
    // rely on jwproxy's intelligence to know what we're talking about and
    // delete the current session
    try {
      await this.jwproxy.command('/', 'DELETE');
    } catch (err) {
      logger.warn(`Did not get confirmation Espresso deleteSession worked; ` +
          `Error was: ${err}`);
    }

    if (this.instProcess && this.instProcess.isRunning) {
      await this.instProcess.stop();
    }
  }
}

export { EspressoRunner, REQUIRED_PARAMS, TEST_APK_PKG };
export default EspressoRunner;
