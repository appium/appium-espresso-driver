import { JWProxy } from 'appium-base-driver';
import { retryInterval } from 'asyncbox';
import logger from './logger';
import path from 'path';
import { fs, util, mkdirp } from 'appium-support';
import { version } from '../../package.json'; // eslint-disable-line import/no-unresolved


const TEST_APK_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'app', 'build', 'outputs', 'apk', 'androidTest', 'debug', 'app-debug-androidTest.apk');
const TEST_MANIFEST_PATH = path.resolve(__dirname, '..', '..', 'espresso-server', 'AndroidManifest-test.xml');
const TEST_APK_PKG = 'io.appium.espressoserver.test';
const REQUIRED_PARAMS = ['adb', 'tmpDir', 'host', 'systemPort', 'devicePort', 'appPackage', 'forceEspressoRebuild'];
const ESPRESSO_SERVER_LAUNCH_TIMEOUT = 30000;

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
  }

  async installTestApk () {
    if (this.forceEspressoRebuild && await fs.exists(this.modServerPath)) {
      logger.debug(`Capability 'forceEspressoRebuild' on. Deleting file '${this.modServerPath}'`);
      await fs.unlink(this.modServerPath);
    }

    if (!(await fs.exists(this.modServerPath))) {
      await this.buildNewModServer();
    }
    await this.checkAndSignCert(this.modServerPath);
    if (this.forceEspressoRebuild) {
      logger.info("New server was built, uninstalling any instances of it");
      await this.adb.uninstallApk(TEST_APK_PKG);
    }

    await this.adb.installOrUpgrade(this.modServerPath, TEST_APK_PKG);
    logger.info(`Installed Espresso Test Server apk '${this.modServerPath}' (pkg: '${TEST_APK_PKG}')`);
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

  // TODO duplicated from uiautomator2, should be a lib method
  async checkAndSignCert (apk, apkPackage) {
    let signed = await this.adb.checkApkCert(apk, apkPackage);
    if (!signed) {
      await this.adb.sign(apk);
    }
    return !signed;
  }

  async startSession (caps) {
    const cmd = [
      'shell',
      'am', 'instrument',
      '-w',
      '-e', 'debug', process.env.ESPRESSO_JAVA_DEBUG === 'true' ? 'true' : 'false',
      `${TEST_APK_PKG}/android.support.test.runner.AndroidJUnitRunner`,
    ];

    logger.info(`Starting Espresso Server v${version} with cmd: adb ${cmd.join(' ')}`);

    // start the instrumentation process
    this.instProcess = this.adb.createSubProcess(cmd);
    this.instProcess.on('exit', (code, signal) => {
      logger.info(`Instrumentation process exited with code ${code} from signal ${signal}`);
    });
    this.instProcess.on('die', (code, signal) => {
      logger.error(`Instrumentation process died with code ${code} and signal ${signal}`);
    });
    this.instProcess.on('stream-line', line => {
      logger.debug(`[Instrumentation]${line.trim()}`);
    });

    await this.instProcess.start((stdout, stderr) => {
      // adb always prints this out on success. If this is found not to be the
      // case, add other conditions
      if (stdout.includes('io.appium.espressoserver.EspressoServerRunnerTest:')) {
        return true;
      }
      if (stderr.includes('Exception')) {
        throw new Error(stderr.trim());
      }
    }, this.serverLaunchTimeout);

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

    if (this.instProcess && this.instProcess.isRunning) {
      await this.instProcess.stop();
    }
  }
}

export { EspressoRunner, REQUIRED_PARAMS };
export default EspressoRunner;
