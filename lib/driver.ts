/* eslint-disable @typescript-eslint/no-non-null-assertion */
import type {
  DefaultCreateSessionResult,
  DriverData,
  ExternalDriver,
  InitialOpts,
  StringRecord,
  SingularSessionData,
  RouteMatcher,
  SessionCapabilities,
} from '@appium/types';
import type {EspressoConstraints} from './constraints';
import _ from 'lodash';
import path from 'node:path';
import B from 'bluebird';
import {errors, isErrorType, DeviceSettings, BaseDriver} from 'appium/driver';
import {EspressoRunner, TEST_APK_PKG} from './espresso-runner';
import {fs, tempDir, zip} from 'appium/support';
import * as appManagementCmds from './commands/app-management';
import * as contextCmds from './commands/context';
import * as elementCmds from './commands/element';
import * as miscCmds from './commands/misc';
import * as servicesCmds from './commands/services';
import * as screenshotCmds from './commands/screenshot';
import * as idlingResourcesCmds from './commands/idling-resources';
import * as actionsCmds from './commands/actions';
import * as clipboardCmds from './commands/clipboard';
import {DEFAULT_ADB_PORT} from 'appium-adb';
import {AndroidDriver, utils} from 'appium-android-driver';
import {SETTINGS_HELPER_ID} from 'io.appium.settings';
import {ESPRESSO_CONSTRAINTS} from './constraints';
import {findAPortNotInUse} from 'portscanner';
import {retryInterval} from 'asyncbox';
import {qualifyActivityName, getPackageInfo} from './utils';
import {newMethodMap} from './method-map';
import type {EspressoDriverCaps, EspressoDriverOpts, W3CEspressoDriverCaps} from './types';
import {executeMethodMap} from './execute-method-map';

// The range of ports we can use on the system for communicating to the
// Espresso HTTP server on the device
const SYSTEM_PORT_RANGE = [8300, 8399];

// This is the port that the espresso server listens to on the device. We will
// forward one of the ports above on the system to this port on the device.
const DEVICE_PORT = 6791;

// NO_PROXY contains the paths that we never want to proxy to espresso server.
// TODO:  Add the list of paths that we never want to proxy to espresso server.
// TODO: Need to segregate the paths better way using regular expressions wherever applicable.
// (Not segregating right away because more paths to be added in the NO_PROXY list)
const NO_PROXY: RouteMatcher[] = [
  ['GET', new RegExp('^/session/(?!.*/)')],
  ['GET', new RegExp('^/session/[^/]+/appium/capabilities')],
  ['GET', new RegExp('^/session/[^/]+/appium/commands')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/current_activity')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/current_package')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/display_density')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/is_keyboard_shown')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/system_bars')],
  ['GET', new RegExp('^/session/[^/]+/appium/device/system_time')],
  ['GET', new RegExp('^/session/[^/]+/appium/extensions')],
  ['GET', new RegExp('^/session/[^/]+/appium/settings')],
  ['GET', new RegExp('^/session/[^/]+/context')],
  ['GET', new RegExp('^/session/[^/]+/contexts')],
  ['GET', new RegExp('^/session/[^/]+/ime/[^/]+')],
  ['GET', new RegExp('^/session/[^/]+/network_connection')],
  ['GET', new RegExp('^/session/[^/]+/timeouts')],
  ['GET', new RegExp('^/session/[^/]+/url')],
  ['POST', new RegExp('^/session/[^/]+/appium/app/background')],
  ['POST', new RegExp('^/session/[^/]+/appium/app/close')],
  ['POST', new RegExp('^/session/[^/]+/appium/app/launch')],
  ['POST', new RegExp('^/session/[^/]+/appium/app/reset')],
  ['POST', new RegExp('^/session/[^/]+/appium/app/strings')],
  ['POST', new RegExp('^/session/[^/]+/appium/compare_images')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/activate_app')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/app_installed')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/app_state')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/finger_print')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/get_clipboard')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/install_app')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/is_locked')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/lock')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/pull_file')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/pull_folder')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/push_file')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/remove_app')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/start_activity')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/terminate_app')],
  ['POST', new RegExp('^/session/[^/]+/appium/device/unlock')],
  ['POST', new RegExp('^/session/[^/]+/appium/getPerformanceData')],
  ['POST', new RegExp('^/session/[^/]+/appium/performanceData/types')],
  ['POST', new RegExp('^/session/[^/]+/appium/settings')],
  ['POST', new RegExp('^/session/[^/]+/appium/execute_driver')],
  ['POST', new RegExp('^/session/[^/]+/appium/start_recording_screen')],
  ['POST', new RegExp('^/session/[^/]+/appium/stop_recording_screen')],
  ['POST', new RegExp('^/session/[^/]+/context')],
  ['POST', new RegExp('^/session/[^/]+/execute')],
  ['POST', new RegExp('^/session/[^/]+/execute/async')],
  ['POST', new RegExp('^/session/[^/]+/execute/sync')],
  ['POST', new RegExp('^/session/[^/]+/execute_async')],
  ['POST', new RegExp('^/session/[^/]+/ime/[^/]+')],
  ['POST', new RegExp('^/session/[^/]+/location')],
  ['POST', new RegExp('^/session/[^/]+/network_connection')],
  ['POST', new RegExp('^/session/[^/]+/timeouts')],
  ['POST', new RegExp('^/session/[^/]+/url')],

  // MJSONWP commands
  ['GET', new RegExp('^/session/[^/]+/log/types')],
  ['POST', new RegExp('^/session/[^/]+/log')],

  // W3C commands
  // For Selenium v4 (W3C does not have this route)
  ['GET', new RegExp('^/session/[^/]+/se/log/types')],
  // For Selenium v4 (W3C does not have this route)
  ['POST', new RegExp('^/session/[^/]+/se/log')],
];

// This is a set of methods and paths that we never want to proxy to Chromedriver.
const CHROME_NO_PROXY: RouteMatcher[] = [
  ['GET', new RegExp('^/session/[^/]+/appium')],
  ['GET', new RegExp('^/session/[^/]+/context')],
  ['GET', new RegExp('^/session/[^/]+/element/[^/]+/rect')],
  ['GET', new RegExp('^/session/[^/]+/orientation')],
  ['POST', new RegExp('^/session/[^/]+/appium')],
  ['POST', new RegExp('^/session/[^/]+/context')],
  ['POST', new RegExp('^/session/[^/]+/orientation')],
  ['POST', new RegExp('^/session/[^/]+/touch/multi/perform')],
  ['POST', new RegExp('^/session/[^/]+/touch/perform')],

  // this is needed to make the mobile: commands working in web context
  ['POST', new RegExp('^/session/[^/]+/execute$')],
  ['POST', new RegExp('^/session/[^/]+/execute/sync')],

  // MJSONWP commands
  ['GET', new RegExp('^/session/[^/]+/log/types')],
  ['POST', new RegExp('^/session/[^/]+/log')],

  // W3C commands
  // For Selenium v4 (W3C does not have this route)
  ['GET', new RegExp('^/session/[^/]+/se/log/types')],
  // For Selenium v4 (W3C does not have this route)
  ['POST', new RegExp('^/session/[^/]+/se/log')],
];

const APK_EXT = '.apk';
const AAB_EXT = '.aab';
const SUPPORTED_EXTENSIONS = [APK_EXT, AAB_EXT];

export class EspressoDriver
  extends AndroidDriver
  implements ExternalDriver<EspressoConstraints, string, StringRecord>
{
  _originalIme: string | null;

  espresso: EspressoRunner;

  wasAnimationEnabled?: boolean;

  static newMethodMap = newMethodMap;
  static executeMethodMap = executeMethodMap;

  override caps: EspressoDriverCaps;

  override opts: EspressoDriverOpts;

  override desiredCapConstraints: EspressoConstraints;

  constructor(opts: InitialOpts = {} as InitialOpts, shouldValidateCaps = true) {
    // `shell` overwrites adb.shell, so remove
    // @ts-expect-error FIXME: what is this?
    delete opts.shell;

    super(opts, shouldValidateCaps);
    this.locatorStrategies = ['id', 'class name', 'accessibility id'];
    this.desiredCapConstraints = ESPRESSO_CONSTRAINTS;
    this.jwpProxyAvoid = NO_PROXY;
    this._originalIme = null;

    this.caps = {} as EspressoDriverCaps;
    this.opts = opts as EspressoDriverOpts;
    this.settings = new DeviceSettings({}, this.onSettingsUpdate.bind(this));

    this.chromedriver = undefined;
  }

  override async getSession(): Promise<SingularSessionData<EspressoConstraints>> {
    return await BaseDriver.prototype.getSession.call(this);
  }

  // needed to make the typechecker happy
  async getAppiumSessionCapabilities(): Promise<SessionCapabilities<EspressoConstraints>> {
    return (await super.getAppiumSessionCapabilities()) as SessionCapabilities<EspressoConstraints>;
  }

  async createSession(
    w3cCaps1: W3CEspressoDriverCaps,
    w3cCaps2?: W3CEspressoDriverCaps,
    w3cCaps3?: W3CEspressoDriverCaps,
    driverData?: DriverData[],
  ): Promise<any> {
    try {
      const [sessionId, caps] = (await BaseDriver.prototype.createSession.call(
        this,
        w3cCaps1,
        w3cCaps2,
        w3cCaps3,
        driverData,
      )) as DefaultCreateSessionResult<EspressoConstraints>;

      const serverDetails = {
        platform: 'LINUX',
        webStorageEnabled: false,
        takesScreenshot: true,
        javascriptEnabled: true,
        databaseEnabled: false,
        networkConnectionEnabled: true,
        locationContextEnabled: false,
        warnings: {},
        desired: Object.assign({}, this.caps),
      };

      this.caps = Object.assign(serverDetails, this.caps);

      this.curContext = (this as unknown as AndroidDriver).defaultContextName();

      const defaultOpts = {
        fullReset: false,
        autoLaunch: true,
        adbPort: DEFAULT_ADB_PORT,
        androidInstallTimeout: 90000,
      };
      _.defaults(this.opts, defaultOpts);

      if (this.isChromeSession) {
        if (this.opts.app) {
          this.log.warn(`'browserName' capability will be ignored`);
          this.log.warn(
            `Chrome browser cannot be run in Espresso sessions because Espresso automation doesn't ` +
              `have permission to access Chrome`,
          );
        } else {
          throw this.log.errorWithException(
            `Chrome browser sessions cannot be run in Espresso because Espresso ` +
              `automation doesn't have permission to access Chrome`,
          );
        }
      }

      this.opts.systemPort =
        this.opts.systemPort ||
        (await findAPortNotInUse(SYSTEM_PORT_RANGE[0], SYSTEM_PORT_RANGE[1]));
      this.opts.adbPort = this.opts.adbPort || DEFAULT_ADB_PORT;
      // get device udid for this session
      const {udid, emPort} = await (this as unknown as AndroidDriver).getDeviceInfoFromCaps();
      this.opts.udid = udid;
      // @ts-expect-error do not put random stuff on opts
      this.opts.emPort = emPort;
      // now that we know our java version and device info, we can create our
      // ADB instance
      this.adb = await (this as unknown as AndroidDriver).createADB();

      if (this.opts.app) {
        // find and copy, or download and unzip an app url or path
        this.opts.app = await this.helpers.configureApp(this.opts.app, {
          onPostProcess: this.onPostConfigureApp.bind(this),
          supportedExtensions: SUPPORTED_EXTENSIONS,
        });
      } else if (this.appOnDevice) {
        // the app isn't an actual app file but rather something we want to
        // assume is on the device and just launch via the appPackage
        this.log.info(
          `App file was not listed, instead we're going to run ` +
            `${this.opts.appPackage} directly on the device`,
        );
        if (!(await this.adb.isAppInstalled(this.opts.appPackage!))) {
          throw this.log.errorWithException(
            `Could not find the package '${this.opts.appPackage}' installed on the device`,
          );
        }
      }

      await this.startEspressoSession();
      return [sessionId, caps];
    } catch (e) {
      await this.deleteSession();
      e.message +=
        `${_.endsWith(e.message, '.') ? '' : '.'} Check ` +
        'https://github.com/appium/appium-espresso-driver#troubleshooting ' +
        'regarding advanced session startup troubleshooting.';
      if (isErrorType(e, errors.SessionNotCreatedError)) {
        throw e;
      }
      const err = new errors.SessionNotCreatedError(e.message);
      err.stack = e.stack;
      throw err;
    }
  }

  /**
   * Unzip the given app path and return the first package that has SUPPORTED_EXTENSIONS
   * in the archived file.
   *
   * @param {string} appPath The path to app file.
   * @returns {Promise<string>} Returns the path to an unzipped app file path.
   * @throws Raise an exception if the zip did not have any SUPPORTED_EXTENSIONS packages.
   */
  async unzipApp(appPath: string): Promise<string> {
    const useSystemUnzipEnv = process.env.APPIUM_PREFER_SYSTEM_UNZIP;
    const useSystemUnzip =
      _.isEmpty(useSystemUnzipEnv) || !['0', 'false'].includes(_.toLower(useSystemUnzipEnv));
    const tmpRoot = await tempDir.openDir();
    await zip.extractAllTo(appPath, tmpRoot, {useSystemUnzip});

    const globPattern = `**/*.+(${SUPPORTED_EXTENSIONS.map((ext) => ext.replace(/^\./, '')).join('|')})`;
    const sortedBundleItems = (
      await fs.glob(globPattern, {
        cwd: tmpRoot,
      })
    ).sort((a, b) => a.split(path.sep).length - b.split(path.sep).length);
    if (sortedBundleItems.length === 0) {
      // no expected packages in the zip
      throw this.log.errorWithException(
        `${this.opts.app} did not have any of '${SUPPORTED_EXTENSIONS.join(', ')}' ` +
          `extension packages. Please make sure the provided .zip archive contains at ` +
          `least one valid application package.`,
      );
    }
    const unzippedAppPath = path.join(tmpRoot, _.first(sortedBundleItems)!);
    this.log.debug(`'${unzippedAppPath}' is the unzipped file from '${appPath}'`);
    return unzippedAppPath;
  }

  async onPostConfigureApp({cachedAppInfo, isUrl, appPath}) {
    const presignApp = async (appLocation) => {
      if (this.opts.noSign) {
        this.log.info(
          'Skipping application signing because noSign capability is set to true. ' +
            'Having the application under test with improper signature/non-signed will cause ' +
            'Espresso automation startup failure.',
        );
      } else if (!(await this.adb.checkApkCert(appLocation, this.opts.appPackage!))) {
        await this.adb.sign(appLocation);
      }
    };

    const hasApkExt = (appPath) => _.endsWith(_.toLower(appPath), APK_EXT);
    const hasAabExt = (appPath) => _.endsWith(_.toLower(appPath), AAB_EXT);
    const extractUniversalApk = async (shouldExtract, appPath) =>
      shouldExtract ? appPath : await this.adb.extractUniversalApk(appPath);

    let pathInCache = null;
    let isResultAppPathAlreadyCached = false;
    if (_.isPlainObject(cachedAppInfo)) {
      const packageHash = await fs.hash(appPath);
      if (packageHash === cachedAppInfo.packageHash && (await fs.exists(cachedAppInfo.fullPath))) {
        this.log.info(`Using '${cachedAppInfo.fullPath}' which is cached from '${appPath}'`);
        isResultAppPathAlreadyCached = true;
        pathInCache = cachedAppInfo.fullPath;
      }
    }

    // appPath can be .zip, .apk or .aab
    const isApk = hasApkExt(appPath);
    // Only local .apk files that are available in-place should not be cached
    const shouldResultAppPathBeCached = !isApk || (isApk && isUrl);

    if (!isResultAppPathAlreadyCached) {
      if (shouldResultAppPathBeCached) {
        // .zip, .aab or downloaded .apk

        let unzippedAppPath;
        let isUnzippedApk = false;
        if (!(hasApkExt(appPath) || hasAabExt(appPath))) {
          unzippedAppPath = await this.unzipApp(appPath);
          isUnzippedApk = hasApkExt(unzippedAppPath);
        }

        // unzippedAppPath or appPath has SUPPORTED_EXTENSIONS.
        pathInCache = unzippedAppPath
          ? await extractUniversalApk(isUnzippedApk, unzippedAppPath)
          : await extractUniversalApk(isApk, appPath);

        if (!isApk && isUrl) {
          // Clean up the temporarily downloaded .aab or .zip package
          await fs.rimraf(appPath);
        }
        if (hasAabExt(unzippedAppPath)) {
          // Cleanup the local unzipped .aab file
          await fs.rimraf(/** @type {string} */ unzippedAppPath);
        }
        await presignApp(pathInCache);
      } else if (isApk) {
        // It is probably not the best idea to modify the provided app in-place,
        // but this is how it was always working
        await presignApp(appPath);
      }
    }
    return shouldResultAppPathBeCached ? {appPath: pathInCache} : false;
  }

  get driverData() {
    // TODO fill out resource info here
    return {};
  }

  // TODO much of this logic is duplicated from uiautomator2
  async startEspressoSession(): Promise<void> {
    const {manifestPayload} = await getPackageInfo();
    this.log.info(`EspressoDriver version: ${manifestPayload.version}`);

    // Read https://github.com/appium/appium-android-driver/pull/461 what happens if there is no setHiddenApiPolicy for Android P+
    if ((await this.adb.getApiLevel()) >= 28) {
      // Android P
      this.log.warn('Relaxing hidden api policy');
      await this.adb.setHiddenApiPolicy('1', !!this.opts.ignoreHiddenApiPolicyError);
    }

    // get appPackage et al from manifest if necessary
    let appInfo = await (this as unknown as AndroidDriver).getLaunchInfo();
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
    await (this as unknown as AndroidDriver).initDevice();

    // Default state is window animation disabled.
    await this.setWindowAnimationState(this.caps.disableWindowAnimation === false);

    // set actual device name, udid
    this.caps.deviceName = this.adb.curDeviceId;
    this.caps.deviceUDID = this.opts.udid!;

    // set up the modified espresso server etc
    this.initEspressoServer();
    // Further prepare the device by forwarding the espresso port
    this.log.debug(`Forwarding Espresso Server port ${DEVICE_PORT} to ${this.opts.systemPort}`);
    await this.adb.forwardPort(this.opts.systemPort!, DEVICE_PORT);

    if (!this.opts.skipUnlock) {
      // unlock the device to prepare it for testing
      // @ts-ignore This is ok
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
    if (!this.caps.appWaitPackage) {
      this.caps.appWaitPackage =
        appInfo.appWaitPackage || appInfo.appPackage || this.caps.appPackage;
    }
    if (this.caps.appActivity) {
      this.caps.appActivity = qualifyActivityName(this.caps.appActivity, this.caps.appPackage!);
    } else {
      this.caps.appActivity = qualifyActivityName(appInfo.appActivity!, this.caps.appPackage!);
    }
    if (this.caps.appWaitActivity) {
      this.caps.appWaitActivity = qualifyActivityName(
        this.caps.appWaitActivity,
        this.caps.appWaitPackage!,
      );
    } else {
      this.caps.appWaitActivity = qualifyActivityName(
        appInfo.appWaitActivity || appInfo.appActivity || this.caps.appActivity,
        this.caps.appWaitPackage!,
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
        this.caps.appWaitPackage!,
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
   * Turn on or off animation scale.
   * '--no-window-animation' instrument argument for Espresso disables window animations,
   * but it does not bring the animation scale back to the pre-instrument process start state in Espresso
   * unlike Appium UIA2 driver case. We want to disable/enable the animation scale only in an appium espresso session as possible.
   * @param isEnabled
   */
  async setWindowAnimationState(isEnabled: boolean): Promise<void> {
    const isAnimationOn = await this.adb.isAnimationOn();
    const shouldDisableAnimation = !isEnabled && isAnimationOn;
    const shouldEnableAnimation = isEnabled && !isAnimationOn;

    if (shouldDisableAnimation) {
      this.log.debug(
        'Disabling window animation as "disableWindowAnimation" capability is set to true/fallback to default value "true"',
      );
      await this.settingsApp.setAnimationState(false);
      this.wasAnimationEnabled = true;
    } else if (shouldEnableAnimation) {
      this.log.debug(
        'Enabling window animation as "disableWindowAnimation" capability is set to false',
      );
      await this.settingsApp.setAnimationState(true);
      this.wasAnimationEnabled = false;
    } else {
      this.log.debug(`Window animation is already ${isEnabled ? 'enabled' : 'disabled'}`);
    }
  }

  async initWebview(): Promise<void> {
    const viewName = (this as unknown as AndroidDriver).defaultWebviewName();
    const timeout = this.opts.autoWebviewTimeout || 2000;
    this.log.info(`Setting webview to context '${viewName}' with timeout ${timeout}ms`);
    await retryInterval(timeout / 500, 500, this.setContext.bind(this), viewName);
  }

  async addDeviceInfoToCaps(): Promise<void> {
    const {apiVersion, platformVersion, manufacturer, model, realDisplaySize, displayDensity} =
      await this.mobileGetDeviceInfo();
    this.caps.deviceApiLevel = parseInt(apiVersion, 10);
    this.caps.platformVersion = platformVersion;
    this.caps.deviceScreenSize = realDisplaySize;
    this.caps.deviceScreenDensity = displayDensity;
    this.caps.deviceModel = model;
    this.caps.deviceManufacturer = manufacturer;
  }

  initEspressoServer(): void {
    // now that we have package and activity, we can create an instance of
    // espresso with the appropriate data
    this.espresso = new EspressoRunner(this.log, {
      host: this.opts.remoteAdbHost || '127.0.0.1',
      systemPort: this.opts.systemPort!,
      devicePort: DEVICE_PORT,
      adb: this.adb,
      tmpDir: this.opts.tmpDir!,
      appPackage: this.opts.appPackage!,
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

  async initAUT(): Promise<void> {
    // Uninstall any uninstallOtherPackages which were specified in caps
    if (this.opts.uninstallOtherPackages) {
      await (this as unknown as AndroidDriver).uninstallOtherPackages(
        utils.parseArray(this.opts.uninstallOtherPackages),
        [SETTINGS_HELPER_ID, TEST_APK_PKG],
      );
    }

    if (!this.opts.app) {
      if (this.opts.fullReset) {
        throw this.log.errorWithException(
          'Full reset requires an app capability, use fastReset if app is not provided',
        );
      }
      this.log.debug('No app capability. Assuming it is already on the device');
      if (this.opts.fastReset) {
        await (this as unknown as AndroidDriver).resetAUT();
      }
    }

    if (!this.opts.skipUninstall) {
      await this.adb.uninstallApk(this.opts.appPackage!);
    }
    if (this.opts.app) {
      await (this as unknown as AndroidDriver).installAUT();
    }
    if (this.opts.skipServerInstallation) {
      this.log.debug('skipServerInstallation capability is set. Not installig espresso-server ');
    } else {
      await this.espresso.installTestApk();
      try {
        await this.adb.addToDeviceIdleWhitelist(SETTINGS_HELPER_ID, TEST_APK_PKG);
      } catch (e) {
        this.log.warn(
          `Cannot add server packages to the Doze whitelist. Original error: ` +
            (e.stderr || e.message),
        );
      }
    }
  }

  override async deleteSession() {
    this.log.debug('Deleting espresso session');

    const screenRecordingStopTasks = [
      async () => {
        if (!_.isEmpty(this._screenRecordingProperties)) {
          await (this as unknown as AndroidDriver).stopRecordingScreen();
        }
      },
      async () => {
        if (await (this as unknown as AndroidDriver).mobileIsMediaProjectionRecordingRunning()) {
          await (this as unknown as AndroidDriver).mobileStopMediaProjectionRecording();
        }
      },
      async () => {
        if (!_.isEmpty(this._screenStreamingProps)) {
          await (this as unknown as AndroidDriver).mobileStopScreenStreaming();
        }
      },
    ];

    if (this.espresso) {
      if (this.jwpProxyActive) {
        await this.espresso.deleteSession();
      }
      // @ts-ignore This is ok
      this.espresso = null;
    }
    this.jwpProxyActive = false;

    if (this.adb) {
      await B.all(
        screenRecordingStopTasks.map((task) => {
          (async () => {
            try {
              await task();
            } catch {}
          })();
        }),
      );
      if (this.wasAnimationEnabled) {
        try {
          await this.settingsApp.setAnimationState(true);
        } catch (err) {
          this.log.warn(`Unable to reset animation: ${err.message}`);
        }
      }
      if (this._originalIme) {
        try {
          await this.adb.setIME(this._originalIme);
        } catch (e) {
          this.log.warn(`Cannot restore the original IME: ${e.message}`);
        }
      }
      if (!this.isChromeSession && this.opts.appPackage && !this.opts.dontStopAppOnReset) {
        await this.adb.forceStop(this.opts.appPackage);
      }
      if (this.opts.fullReset && !this.opts.skipUninstall && !this.appOnDevice) {
        this.log.debug(`FULL_RESET set to 'true', Uninstalling '${this.opts.appPackage}'`);
        await this.adb.uninstallApk(this.opts.appPackage!);
      }
      if ((await this.adb.getApiLevel()) >= 28) {
        // Android P
        this.log.info('Restoring hidden api policy to the device default configuration');
        await this.adb.setDefaultHiddenApiPolicy(!!this.opts.ignoreHiddenApiPolicyError);
      }
    }
    await super.deleteSession();
    if (this.opts.systemPort) {
      try {
        await this.adb.removePortForward(this.opts.systemPort);
      } catch (error) {
        this.log.warn(`Unable to remove port forward '${error.message}'`);
        //Ignore, this block will also be called when we fall in catch block
        // and before even port forward.
      }
    }
  }

  async onSettingsUpdate() {
    // intentionally do nothing here, since commands.updateSettings proxies
    // settings to the espresso server already
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  proxyActive(sessionId) {
    // we always have an active proxy to the espresso server
    return true;
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  canProxy(sessionId) {
    // we can always proxy to the espresso server
    return true;
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  getProxyAvoidList(sessionId): RouteMatcher[] {
    // we are maintaining two sets of NO_PROXY lists, one for chromedriver(CHROME_NO_PROXY)
    // and one for Espresso(NO_PROXY), based on current context will return related NO_PROXY list
    this.jwpProxyAvoid = _.isNil(this.chromedriver) ? NO_PROXY : CHROME_NO_PROXY;
    if (this.opts.nativeWebScreenshot) {
      this.jwpProxyAvoid = [
        ...this.jwpProxyAvoid,
        ['GET', new RegExp('^/session/[^/]+/screenshot')],
      ];
    }

    return this.jwpProxyAvoid;
  }

  get appOnDevice(): boolean {
    return !this.opts.app && this.helpers.isPackageOrBundle(this.opts.appPackage!);
  }

  // @ts-ignore It's expected
  performActions = actionsCmds.performActions;

  startActivity = appManagementCmds.startActivity;
  // @ts-ignore It's expected
  mobileStartActivity = appManagementCmds.mobileStartActivity;

  mobileWebAtoms = contextCmds.mobileWebAtoms;
  // @ts-ignore It's expected
  suspendChromedriverProxy = contextCmds.suspendChromedriverProxy;

  // @ts-ignore It's expected
  mobilePerformEditorAction = elementCmds.mobilePerformEditorAction;
  mobileSwipe = elementCmds.mobileSwipe;
  mobileOpenDrawer = elementCmds.mobileOpenDrawer;
  mobileCloseDrawer = elementCmds.mobileCloseDrawer;
  mobileSetDate = elementCmds.mobileSetDate;
  mobileSetTime = elementCmds.mobileSetTime;
  mobileNavigateTo = elementCmds.mobileNavigateTo;
  mobileScrollToPage = elementCmds.mobileScrollToPage;
  mobileFlashElement = elementCmds.mobileFlashElement;
  mobileClickAction = elementCmds.mobileClickAction;
  mobileDismissAutofill = elementCmds.mobileDismissAutofill;

  mobilePressKey = miscCmds.mobilePressKey;
  mobileGetDeviceInfo = miscCmds.mobileGetDeviceInfo;
  mobileIsToastVisible = miscCmds.mobileIsToastVisible;
  // @ts-ignore It's expected
  getDisplayDensity = miscCmds.getDisplayDensity;
  mobileBackdoor = miscCmds.mobileBackdoor;
  mobileUiautomator = miscCmds.mobileUiautomator;
  mobileUiautomatorPageSource = miscCmds.mobileUiautomatorPageSource;
  updateSettings = miscCmds.updateSettings;
  getSettings = miscCmds.getSettings;

  getClipboard = clipboardCmds.getClipboard;
  mobileGetClipboard = clipboardCmds.getClipboard;
  mobileSetClipboard = clipboardCmds.mobileSetClipboard;

  // @ts-ignore It's expected
  mobileStartService = servicesCmds.mobileStartService;
  // @ts-ignore It's expected
  mobileStopService = servicesCmds.mobileStopService;

  getScreenshot = screenshotCmds.getScreenshot;
  mobileScreenshots = screenshotCmds.mobileScreenshots;

  mobileRegisterIdlingResources = idlingResourcesCmds.mobileRegisterIdlingResources;
  mobileUnregisterIdlingResources = idlingResourcesCmds.mobileUnregisterIdlingResources;
  mobileListIdlingResources = idlingResourcesCmds.mobileListIdlingResources;
  mobileWaitForUIThread = idlingResourcesCmds.mobileWaitForUIThread;
}

export default EspressoDriver;
