import type {
  DefaultCreateSessionResult,
  DriverData,
  ExternalDriver,
  InitialOpts,
  RouteMatcher,
  StringRecord,
  SingularSessionData,
  SessionCapabilities,
} from '@appium/types';
import type {EspressoConstraints} from './constraints';
import {errors, isErrorType, DeviceSettings, BaseDriver} from 'appium/driver';
import * as serverCmds from './commands/server';
import type {EspressoRunner} from './commands/server';
import * as appManagementCmds from './commands/app-management';
import * as contextCmds from './commands/context';
import * as elementCmds from './commands/element';
import * as miscCmds from './commands/misc';
import * as servicesCmds from './commands/services';
import * as screenshotCmds from './commands/screenshot';
import * as idlingResourcesCmds from './commands/idling-resources';
import * as actionsCmds from './commands/actions';
import * as clipboardCmds from './commands/clipboard';
import * as appInstallCmds from './commands/app-install';
import {DEFAULT_ADB_PORT} from 'appium-adb';
import {AndroidDriver} from 'appium-android-driver';
import {ESPRESSO_CONSTRAINTS} from './constraints';
import {findAPortNotInUse} from 'portscanner';
import {retryInterval} from 'asyncbox';
import {isEmptyValue} from './utils';
import {newMethodMap} from './method-map';
import type {EspressoDriverCaps, EspressoDriverOpts, W3CEspressoDriverCaps} from './types';
import {executeMethodMap} from './execute-method-map';

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

export class EspressoDriver
  extends AndroidDriver
  implements ExternalDriver<EspressoConstraints, string, StringRecord>
{
  static newMethodMap = newMethodMap;
  static executeMethodMap = executeMethodMap as unknown as typeof AndroidDriver.executeMethodMap;

  _originalIme: string | null;

  espresso!: EspressoRunner;

  wasAnimationEnabled?: boolean;

  override caps: EspressoDriverCaps;

  override opts: EspressoDriverOpts;

  override desiredCapConstraints: EspressoConstraints;

  performActions = actionsCmds.performActions as AndroidDriver['performActions'];

  startActivity = appManagementCmds.startActivity;
  mobileStartActivity =
    appManagementCmds.mobileStartActivity as unknown as AndroidDriver['mobileStartActivity'];

  mobileWebAtoms = contextCmds.mobileWebAtoms;
  suspendChromedriverProxy =
    contextCmds.suspendChromedriverProxy as AndroidDriver['suspendChromedriverProxy'];

  mobilePerformEditorAction =
    elementCmds.mobilePerformEditorAction as AndroidDriver['mobilePerformEditorAction'];
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
  getDisplayDensity = miscCmds.getDisplayDensity as AndroidDriver['getDisplayDensity'];
  mobileBackdoor = miscCmds.mobileBackdoor;
  mobileUiautomator = miscCmds.mobileUiautomator;
  mobileUiautomatorPageSource = miscCmds.mobileUiautomatorPageSource;
  updateSettings = miscCmds.updateSettings;
  getSettings = miscCmds.getSettings;

  getClipboard = clipboardCmds.getClipboard;
  mobileGetClipboard = clipboardCmds.getClipboard;
  mobileSetClipboard = clipboardCmds.mobileSetClipboard;

  mobileStartService =
    servicesCmds.mobileStartService as unknown as AndroidDriver['mobileStartService'];
  mobileStopService =
    servicesCmds.mobileStopService as unknown as AndroidDriver['mobileStopService'];

  getScreenshot = screenshotCmds.getScreenshot;
  mobileScreenshots = screenshotCmds.mobileScreenshots;

  mobileRegisterIdlingResources = idlingResourcesCmds.mobileRegisterIdlingResources;
  mobileUnregisterIdlingResources = idlingResourcesCmds.mobileUnregisterIdlingResources;
  mobileListIdlingResources = idlingResourcesCmds.mobileListIdlingResources;
  mobileWaitForUIThread = idlingResourcesCmds.mobileWaitForUIThread;

  startEspressoSession = serverCmds.startSession;
  initEspressoServer = serverCmds.initServer;

  unzipApp = appInstallCmds.unzipApp;
  onPostConfigureApp = appInstallCmds.onPostConfigureApp;
  initAUT = appInstallCmds.initAUT;

  constructor(opts: InitialOpts = {} as InitialOpts, shouldValidateCaps = true) {
    // `shell` overwrites adb.shell, so remove
    if ('shell' in opts) {
      delete (opts as {shell?: unknown}).shell;
    }

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

  get driverData() {
    return {};
  }

  get appOnDevice(): boolean {
    return appInstallCmds.isAppOnDevice(this);
  }

  override async getSession(): Promise<SingularSessionData<EspressoConstraints>> {
    return await BaseDriver.prototype.getSession.call(this);
  }

  // needed to make the typechecker happy
  async getAppiumSessionCapabilities(): Promise<SessionCapabilities<EspressoConstraints>> {
    return (await super.getAppiumSessionCapabilities()) as SessionCapabilities<EspressoConstraints>;
  }

  override async createSession(
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

      this.curContext = this.defaultContextName();

      const defaultOpts = {
        fullReset: false,
        autoLaunch: true,
        adbPort: DEFAULT_ADB_PORT,
        androidInstallTimeout: 90000,
      };
      this.opts = Object.assign({}, defaultOpts, this.opts);

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
        (await findAPortNotInUse(serverCmds.SYSTEM_PORT_RANGE[0], serverCmds.SYSTEM_PORT_RANGE[1]));
      this.opts.adbPort = this.opts.adbPort || DEFAULT_ADB_PORT;
      // get device udid for this session
      const {udid, emPort} = await this.getDeviceInfoFromCaps();
      this.opts.udid = udid;
      (this.opts as EspressoDriverOpts & {emPort: typeof emPort}).emPort = emPort;
      // now that we know our java version and device info, we can create our
      // ADB instance
      this.adb = await this.createADB();

      if (this.opts.app) {
        // find and copy, or download and unzip an app url or path
        this.opts.app = await this.helpers.configureApp(this.opts.app, {
          onPostProcess: this.onPostConfigureApp.bind(this),
          supportedExtensions: appInstallCmds.SUPPORTED_EXTENSIONS,
        });
      } else if (this.appOnDevice) {
        // the app isn't an actual app file but rather something we want to
        // assume is on the device and just launch via the appPackage
        this.log.info(
          `App file was not listed, instead we're going to run ` +
            `${this.opts.appPackage} directly on the device`,
        );
        if (!(await this.adb.isAppInstalled(this.opts.appPackage as string))) {
          throw this.log.errorWithException(
            `Could not find the package '${this.opts.appPackage}' installed on the device`,
          );
        }
      }

      await this.startEspressoSession();
      return [sessionId, caps];
    } catch (e: unknown) {
      await this.deleteSession();
      const cause = e instanceof Error ? e : new Error(String(e));
      cause.message +=
        `${cause.message.endsWith('.') ? '' : '.'} Check ` +
        'https://github.com/appium/appium-espresso-driver#troubleshooting ' +
        'regarding advanced session startup troubleshooting.';
      if (isErrorType(e, errors.SessionNotCreatedError)) {
        throw e;
      }
      throw new errors.SessionNotCreatedError(cause.message, cause as Error);
    }
  }

  override async deleteSession() {
    this.log.debug('Deleting espresso session');

    const screenRecordingStopTasks = [
      async () => {
        if (!isEmptyValue(this._screenRecordingProperties)) {
          await this.stopRecordingScreen();
        }
      },
      async () => {
        if (await this.mobileIsMediaProjectionRecordingRunning()) {
          await this.mobileStopMediaProjectionRecording();
        }
      },
      async () => {
        if (!isEmptyValue(this._screenStreamingProps)) {
          await this.mobileStopScreenStreaming();
        }
      },
    ];

    await serverCmds.teardown(this);

    if (this.adb) {
      await Promise.all(
        screenRecordingStopTasks.map((task) =>
          (async () => {
            try {
              await task();
            } catch {}
          })(),
        ),
      );
      if (this.wasAnimationEnabled) {
        try {
          await this.settingsApp.setAnimationState(true);
        } catch (err: unknown) {
          const message = err instanceof Error ? err.message : String(err);
          this.log.warn(`Unable to reset animation: ${message}`);
        }
      }
      if (this._originalIme) {
        try {
          await this.adb.setIME(this._originalIme);
        } catch (e: unknown) {
          const message = e instanceof Error ? e.message : String(e);
          this.log.warn(`Cannot restore the original IME: ${message}`);
        }
      }
      if (!this.isChromeSession && this.opts.appPackage && !this.opts.dontStopAppOnReset) {
        await this.adb.forceStop(this.opts.appPackage);
      }
      if (this.opts.fullReset && !this.opts.skipUninstall && !this.appOnDevice) {
        this.log.debug(`FULL_RESET set to 'true', Uninstalling '${this.opts.appPackage}'`);
        await this.adb.uninstallApk(this.opts.appPackage as string);
      }
      if ((await this.adb.getApiLevel()) >= 28) {
        // Android P
        this.log.info('Restoring hidden api policy to the device default configuration');
        await this.adb.setDefaultHiddenApiPolicy(!!this.opts.ignoreHiddenApiPolicyError);
      }
    }
    await super.deleteSession();
    await serverCmds.removePortForward(this);
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
    const viewName = this.defaultWebviewName();
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

  async onSettingsUpdate() {
    // intentionally do nothing here, since commands.updateSettings proxies
    // settings to the espresso server already
  }

  override proxyActive(sessionId?: string | null) {
    void sessionId;
    // we always have an active proxy to the espresso server
    return true;
  }

  override canProxy(sessionId?: string | null) {
    void sessionId;
    // we can always proxy to the espresso server
    return true;
  }

  override getProxyAvoidList(sessionId?: string | null): RouteMatcher[] {
    void sessionId;
    // we are maintaining two sets of NO_PROXY lists, one for chromedriver(CHROME_NO_PROXY)
    // and one for Espresso(NO_PROXY), based on current context will return related NO_PROXY list
    this.jwpProxyAvoid = this.chromedriver == null ? NO_PROXY : CHROME_NO_PROXY;
    if (this.opts.nativeWebScreenshot) {
      this.jwpProxyAvoid = [
        ...this.jwpProxyAvoid,
        ['GET', new RegExp('^/session/[^/]+/screenshot')],
      ];
    }

    return this.jwpProxyAvoid;
  }
}

export default EspressoDriver;
