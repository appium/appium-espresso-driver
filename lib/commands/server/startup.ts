import type {EspressoDriver} from '../../driver';
import {qualifyActivityName, getPackageInfo} from '../../utils';
import {DEVICE_PORT} from './constants';
import {EspressoRunner} from './runner';

/**
 * Start an Espresso session: device setup, server install, and session launch.
 */
export async function startSession(this: EspressoDriver): Promise<void> {
  const {manifestPayload} = await getPackageInfo();
  this.log.info(`EspressoDriver version: ${manifestPayload.version}`);

  // Read https://github.com/appium/appium-android-driver/pull/461 what happens if there is no setHiddenApiPolicy for Android P+
  if ((await this.adb.getApiLevel()) >= 28) {
    // Android P
    this.log.warn('Relaxing hidden api policy');
    await this.adb.setHiddenApiPolicy('1', !!this.opts.ignoreHiddenApiPolicyError);
  }

  // get appPackage et al from manifest if necessary
  let appInfo = await this.getLaunchInfo();
  if (appInfo) {
    // and get it onto our 'opts' object so we use it from now on
    Object.assign(this.opts, appInfo);
  } else {
    appInfo = this.opts;
  }

  // start an avd, set the language/locale, pick an emulator, etc...
  if (this.opts.hideKeyboard) {
    this._originalIme = await this.adb.defaultIME();
  }
  await this.initDevice();

  // Default state is window animation disabled.
  await this.setWindowAnimationState(this.caps.disableWindowAnimation === false);

  // set actual device name, udid
  this.caps.deviceName = this.adb.curDeviceId;
  const {udid, systemPort} = this.opts;
  if (!udid) {
    throw this.log.errorWithException('udid is required');
  }
  if (systemPort == null) {
    throw this.log.errorWithException('systemPort is required');
  }
  this.caps.deviceUDID = udid;

  // set up the modified espresso server etc
  this.initEspressoServer();
  // Further prepare the device by forwarding the espresso port
  this.log.debug(`Forwarding Espresso Server port ${DEVICE_PORT} to ${systemPort}`);
  await this.adb.forwardPort(systemPort, DEVICE_PORT);

  if (!this.opts.skipUnlock) {
    // unlock the device to prepare it for testing
    await this.unlock();
  } else {
    this.log.debug(`'skipUnlock' capability set, so skipping device unlock`);
  }

  // set up app under test
  // prepare our actual AUT, get it on the device, etc...
  await this.initAUT();

  //Adding AUT package name in the capabilities if package name not exist in caps
  if (!this.caps.appPackage) {
    this.caps.appPackage = appInfo.appPackage;
  }
  const appPackage = this.caps.appPackage;
  if (!appPackage) {
    throw this.log.errorWithException('appPackage is required');
  }
  if (!this.caps.appWaitPackage) {
    this.caps.appWaitPackage = appInfo.appWaitPackage || appInfo.appPackage || appPackage;
  }
  const appWaitPackage = this.caps.appWaitPackage;
  if (!appWaitPackage) {
    throw this.log.errorWithException('appWaitPackage is required');
  }
  if (this.caps.appActivity) {
    this.caps.appActivity = qualifyActivityName(this.caps.appActivity, appPackage);
  } else {
    const appActivity = appInfo.appActivity;
    if (!appActivity) {
      throw this.log.errorWithException('appActivity is required');
    }
    this.caps.appActivity = qualifyActivityName(appActivity, appPackage);
  }
  if (this.caps.appWaitActivity) {
    this.caps.appWaitActivity = qualifyActivityName(this.caps.appWaitActivity, appWaitPackage);
  } else {
    this.caps.appWaitActivity = qualifyActivityName(
      appInfo.appWaitActivity || appInfo.appActivity || this.caps.appActivity,
      appWaitPackage,
    );
  }

  // launch espresso and wait till its online and we have a session
  await this.espresso.startSession(this.caps);
  if (this.caps.autoLaunch === false) {
    this.log.info(
      `Not waiting for the application activity to start because 'autoLaunch' is disabled`,
    );
  } else {
    await this.adb.waitForActivity(
      appWaitPackage,
      this.caps.appWaitActivity,
      this.opts.appWaitDuration,
    );
  }
  // if we want to immediately get into a webview, set our context
  // appropriately
  if (this.opts.autoWebview) {
    await this.initWebview();
  }

  // now that everything has started successfully, turn on proxying so all
  // subsequent session requests go straight to/from espresso
  this.jwpProxyActive = true;

  await this.addDeviceInfoToCaps();
}

/**
 * Create the Espresso runner and wire proxy handlers on the driver.
 */
export function initServer(this: EspressoDriver): void {
  const {systemPort, tmpDir, appPackage} = this.opts;
  if (systemPort == null) {
    throw this.log.errorWithException('systemPort is required');
  }
  if (!tmpDir) {
    throw this.log.errorWithException('tmpDir is required');
  }
  if (!appPackage) {
    throw this.log.errorWithException('appPackage is required');
  }

  // now that we have package and activity, we can create an instance of
  // espresso with the appropriate data
  this.espresso = new EspressoRunner(this.log, {
    host: this.opts.remoteAdbHost || '127.0.0.1',
    systemPort,
    devicePort: DEVICE_PORT,
    adb: this.adb,
    tmpDir,
    appPackage,
    forceEspressoRebuild: !!this.opts.forceEspressoRebuild,
    espressoBuildConfig: this.opts.espressoBuildConfig,
    showGradleLog: !!this.opts.showGradleLog,
    serverLaunchTimeout: this.opts.espressoServerLaunchTimeout,
    androidInstallTimeout: this.opts.androidInstallTimeout,
    useKeystore: this.opts.useKeystore,
    keystorePath: this.opts.keystorePath,
    keystorePassword: this.opts.keystorePassword,
    keyAlias: this.opts.keyAlias,
    keyPassword: this.opts.keyPassword,
    disableSuppressAccessibilityService: this.opts.disableSuppressAccessibilityService,
    reqBasePath: this.basePath,
  });
  this.proxyReqRes = this.espresso.proxyReqRes.bind(this.espresso);
  this.proxyCommand = this.espresso.proxyCommand.bind(this.espresso);
}

/**
 * Tear down the Espresso server session and clear the runner reference.
 */
export async function teardown(driver: EspressoDriver): Promise<void> {
  if (driver.espresso) {
    if (driver.jwpProxyActive) {
      await driver.espresso.deleteSession();
    }
    (driver as {espresso: EspressoRunner | null}).espresso = null;
  }
  driver.jwpProxyActive = false;
}

/**
 * Remove the ADB port forward for the Espresso server system port.
 */
export async function removePortForward(driver: EspressoDriver): Promise<void> {
  if (driver.opts.systemPort) {
    try {
      await driver.adb.removePortForward(driver.opts.systemPort);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      driver.log.warn(`Unable to remove port forward '${message}'`);
      // Ignore, this block will also be called when we fall in catch block
      // and before even port forward.
    }
  }
}
