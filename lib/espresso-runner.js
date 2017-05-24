import { exec } from 'teen_process';
import { JWProxy } from 'appium-base-driver';
import { retryInterval } from 'asyncbox';
import logger from './logger';

const TEST_APK_PATH = ""; // TODO figure out where this will be built
const TEST_APK_PKG = "io.appium.espressoserver.test";
const REQD_PARAMS = ['adb', 'tmpDir', 'host', 'systemPort', 'devicePort'];

class EspressoRunner {
  constructor (opts = {}) {
    for (let req of REQD_PARAMS) {
      if (!opts || !opts[req]) {
        throw new Error(`Option '${req}' is required!`);
      }
      this[req] = opts[req];
    }
    this.jwproxy = new JWProxy({host: this.host, port: this.systemPort});
    this.proxyReqRes = this.jwproxy.proxyReqRes.bind(this.jwproxy);
  }

  // TODO mostly duplicated from uiautomator2's installServerApk
  async installTestApk () {
    // Installs the apks on to the device or emulator
    let isApkInstalled = await this.adb.isAppInstalled(TEST_APK_PKG);
    if (isApkInstalled) {
      //check server apk versionName
      let apkVersion = await this.getAPKVersion(TEST_APK_PATH);
      let pkgVersion = await this.getInstalledPackageVersion(TEST_APK_PKG);
      if (apkVersion !== pkgVersion) {
        isApkInstalled = false;
        await this.adb.uninstallApk(TEST_APK_PKG);
      }
    }
    if (!isApkInstalled) {
      await this.signAndInstall(TEST_APK_PATH, TEST_APK_PKG);
    }
  }

  // TODO duplicated from uiautomator2
  async signAndInstall (apk, apkPackage) {
    await this.checkAndSignCert(apk, apkPackage);
    await this.adb.install(apk);
    logger.info("Installed Espresso Test Server apk");
  }

  // TODO duplicated from uiautomator2, should be a lib method
  async getAPKVersion (apk) {
    let args = ['dump', 'badging', apk];
    await this.adb.initAapt();
    let {stdout} = await exec(this.adb.binaries.aapt, args);
    let apkVersion = new RegExp(/versionName='([^']+)'/g).exec(stdout);
    if (apkVersion && apkVersion.length >= 2) {
      apkVersion = apkVersion[1];
    } else {
      apkVersion = null;
    }
    return apkVersion.toString();
  }

  // TODO duplicated from uiautomator2, should be a lib method
  async getInstalledPackageVersion (pkg) {
    let stdout =  await this.adb.shell(['dumpsys', 'package', pkg]);
    let pkgVersion = new RegExp(/versionName=([^\s\s]+)/g).exec(stdout);
    if (pkgVersion && pkgVersion.length >= 2) {
      pkgVersion = pkgVersion[1];
    } else {
      pkgVersion = null;
    }
    return pkgVersion.toString();
  }

  // TODO duplicated from uiautomator2, should be a lib method
  async checkAndSignCert (apk, apkPackage) {
    let signed = await this.adb.checkApkCert(apk, apkPackage);
    if (!signed) {
      await this.adb.sign(apk);
    }
    return !signed;
  }

  async startSession (caps) {
    let cmd = ['am', 'instrument', '-w', '-e', 'debug',
      `${TEST_APK_PKG}/android.support.test.runner.AndroidJUnitRunner`];

    logger.info(`Starting Espresso Server /*TODO VERSION*/ with cmd: ` +
        `${cmd}`);

    await this.adb.shell(cmd);

    logger.info('Waiting for Espresso to be online...');
    // wait 20s for UiAutomator2 to be online
    await retryInterval(20, 1000, async () => {
      await this.jwproxy.command('/status', 'GET');
    });
    await this.jwproxy.command('/session', 'POST', {desiredCapabilities: caps});
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
  }
}

export default EspressoRunner;
