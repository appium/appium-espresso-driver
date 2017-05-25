import { exec } from 'teen_process';
import { JWProxy } from 'appium-base-driver';
import { retryInterval } from 'asyncbox';
import logger from './logger';
import path from 'path';
import { fs } from 'appium-support';


const TEST_APK_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug', 'app-debug-androidTest.apk');
const APP_APK_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const TEST_MANIFEST_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'AndroidManifest-test.xml');
const TEST_APK_PKG = "io.appium.espressoserver.test";
const APP_APK_PKG = "io.appium.espressoserver";
const REQD_PARAMS = ['adb', 'tmpDir', 'host', 'systemPort', 'devicePort', 'appPackage'];

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

    this.modServerPath = path.resolve(this.tmpDir, `${TEST_APK_PKG}.apk`);
  }

  // TODO mostly duplicated from uiautomator2's installServerApk
  async installTestApk () {
    for (let pkg of [TEST_APK_PKG, APP_APK_PKG]) {
      if (await this.adb.isAppInstalled(pkg)) {
        // for now, uninstall always
        // TODO: figure out how to know if the server is able to instrument the AUT
        await this.adb.uninstallApk(pkg);
      }
    }

    let tmpTestApkPath = await this.buildNewModServer();
    await this.signAndInstall(this.modServerPath, TEST_APK_PKG);

    await this.signAndInstall(APP_APK_PATH, APP_APK_PKG);
  }

  async buildNewModServer () {
    logger.info(`Repackaging espresso server for: '${this.appPackage}'`);
    let packageTmpDir = path.resolve(this.tmpDir, this.appPackage);
    let newManifestPath = path.resolve(this.tmpDir, 'AndroidManifest.xml');
    logger.info(`Creating new manifest: '${newManifestPath}'`);
    await fs.mkdir(packageTmpDir);
    await fs.copyFile(TEST_MANIFEST_PATH, newManifestPath);
    if (await fs.exists(this.modServerPath)) {
      await fs.unlink(this.modServerPath);
    }
    await this.adb.initAapt(); // TODO this should be internal to adb
    await this.adb.compileManifest(newManifestPath, TEST_APK_PKG, this.appPackage); // creates a file `${newManifestPath}.apk`
    await this.adb.insertManifest(newManifestPath, TEST_APK_PATH, this.modServerPath); // copies from second are to third and add manifest
    logger.info(`Repackaged espresso server ready: '${this.modServerPath}'`);
  }

  // TODO duplicated from uiautomator2
  async signAndInstall (apk, apkPackage) {
    await this.checkAndSignCert(apk, apkPackage);
    await this.adb.install(apk);
    logger.info(`Installed Espresso Test Server apk '${apk}' (pkg: '${apkPackage}')`);
  }

  // TODO duplicated from uiautomator2, should be a lib method
  async checkAndSignCert (apk, apkPackage) {
    let signed = await this.adb.checkApkCert(apk, apkPackage);
    if (!signed) {
      await this.adb.sign(apk);
    }
    return !signed;
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

  async startSession (caps) {
    let cmd = ['am', 'instrument', '-w', '-e', 'debug', 'false',
      `${TEST_APK_PKG}/android.support.test.runner.AndroidJUnitRunner`];

    logger.info(`Starting Espresso Server /*TODO VERSION*/ with cmd: ${cmd.join(' ')}`);

    await this.adb.shell(cmd);

    logger.info('Waiting for Espresso to be online...');
    // wait 20s for espresso to be online
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
