import { JWProxy } from 'appium-base-driver';
import { retryInterval } from 'asyncbox';
import logger from './logger';
import path from 'path';
import { fs, util, mkdirp } from 'appium-support';
import { version } from '../../package.json'; // eslint-disable-line import/no-unresolved
import request from 'request-promise';
import B from 'bluebird';
import _ from 'lodash';


const TEST_APK_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug', 'app-debug-androidTest.apk');
const TEST_MANIFEST_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'AndroidManifest-test.xml');
const TEST_APK_PKG = 'io.appium.espressoserver.test';
const REQUIRED_PARAMS = ['adb', 'tmpDir', 'host', 'systemPort', 'devicePort', 'appPackage', 'forceEspressoRebuild'];
const ESPRESSO_SERVER_LAUNCH_TIMEOUT = 30000;
const TARGET_PACKAGE_CONTAINER = '/data/local/tmp/espresso.apppackage';

class EspressoRunner {
  constructor (opts = {}) {
    for (let req of REQUIRED_PARAMS) {
      if (!opts || !util.hasValue(opts[req])) {
        throw new Error(`Option '${req}' is required!`);
      }
      this[req] = opts[req];
    }
    this.jwproxy = new JWProxy({server: this.host, port: this.systemPort, base: ''});
    this.proxyReqRes = this.jwproxy.proxyReqRes.bind(this.jwproxy);

    this.modServerPath = path.resolve(this.tmpDir, `${TEST_APK_PKG}_${version}_${this.appPackage}.apk`);

    this.serverLaunchTimeout = opts.serverLaunchTimeout || ESPRESSO_SERVER_LAUNCH_TIMEOUT;
    this.androidInstallTimeout = opts.androidInstallTimeout;
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
    logger.info(`Repackaging espresso server for: '${this.appPackage}'`);
    const packageTmpDir = path.resolve(this.tmpDir, this.appPackage);
    const newManifestPath = path.resolve(this.tmpDir, 'AndroidManifest.xml');
    await fs.rimraf(newManifestPath);

    logger.info(`Creating new manifest: '${newManifestPath}'`);
    await mkdirp(packageTmpDir);
    await fs.copyFile(TEST_MANIFEST_PATH, newManifestPath);
    await this.adb.compileManifest(newManifestPath, TEST_APK_PKG, this.appPackage); // creates a file `${newManifestPath}.apk`
    await this.adb.insertManifest(newManifestPath, TEST_APK_PATH, this.modServerPath); // copies from second to third and add manifest
    logger.info(`Repackaged espresso server ready: '${this.modServerPath}'`);
  }

  async cleanupSessionLeftovers () {
    logger.debug('Performing cleanup of automation leftovers');

    try {
      const {value} = await request.get({
        url: `http://${this.host}:${this.systemPort}/sessions`,
        timeout: 500,
        json: true,
      });
      const activeSessionIds = value.map((sess) => sess.id);
      if (activeSessionIds.length) {
        logger.debug(`The following obsolete sessions are still running: ${JSON.stringify(activeSessionIds)}`);
        logger.debug('Cleaning up the obsolete sessions');
        await B.all(activeSessionIds.map((id) =>
          request.delete({
            url: `http://${this.host}:${this.systemPort}/session/${id}`,
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
      '-e', 'debug', process.env.ESPRESSO_JAVA_DEBUG === 'true' ? 'true' : 'false',
      `${TEST_APK_PKG}/androidx.test.runner.AndroidJUnitRunner`,
    ];

    logger.info(`Starting Espresso Server v${version} with cmd: adb ${cmd.join(' ')}`);

    let hasSocketError = false;

    // start the instrumentation process
    this.instProcess = this.adb.createSubProcess(cmd);
    this.instProcess.on('exit', (code, signal) => {
      logger.info(`Instrumentation process exited with code ${code} from signal ${signal}`);
    });
    this.instProcess.on('die', (code, signal) => {
      logger.error(`Instrumentation process died with code ${code} and signal ${signal}`);
    });
    this.instProcess.on('stream-line', (line) => {
      if (_.isEmpty(line.trim())) {
        // Do not print empty lines into the system log
        return;
      }

      logger.debug(`[Instrumentation] ${line.trim()}`);
      // A 'SocketException' indicates that we couldn't connect to the Espresso Server, because the INTERNET permission is not set
      if (line.toLowerCase().includes('java.net.socketexception')) {
        hasSocketError = true;
      }
    });

    await this.instProcess.start((stdout, stderr) => {
      // for any call to the start detector, one of stdout or stderr will have
      // content, so merge for checks here
      const out = stdout.trim() || stderr.trim();

      // adb always prints this out on success. If this is found not to be the
      // case, add other conditions
      if (out.includes('io.appium.espressoserver.EspressoServerRunnerTest:')) {
        return true;
      }
      if (out.toLowerCase().includes('exception')) {
        throw new Error(out);
      }
    }, this.serverLaunchTimeout);

    logger.info('Waiting for Espresso to be online...');

    // wait 20s for espresso to be online
    try {
      await retryInterval(20, 1000, async () => {
        await this.jwproxy.command('/status', 'GET');
      });
    } catch (e) {
      if (hasSocketError) {
        logger.errorAndThrow(`Timed out waiting for Espresso Server to start due to Socket exception. Espresso Server requires the 'INTERNET' permission to be set in the Android manifest for the app-under-test (<uses-permission android:name="android.permission.INTERNET" />)`);
      } else {
        logger.errorAndThrow(`Timed out waiting for Espresso Server to start. Original error: ${e.message}`);
      }
    }

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
