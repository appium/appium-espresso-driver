---
title: Capabilities
---

This page lists various capabilities used and implemented by the Espresso driver. To learn more
about capabilities, refer to the [Appium documentation](https://appium.io/docs/en/latest/guides/caps/).

For other capabilities recognized by the Appium server, see
[their Appium docs reference page](https://appium.io/docs/en/latest/reference/session/caps/).

## Standard

Refer to [the W3C WebDriver documentation](https://w3c.github.io/webdriver/#capabilities)
for more information about these capabilities.

### platformName

| Name | Type | Default |
| -- | -- | -- |
| `platformName` | `string` | Not specified |

May be set to `android`. Appium is not strict about this value if the [`appium:automationName`](#automationname)
capability is provided, so feel free to assign it to any platform name required, for example, for
Selenium Grid compatibility.

### pageLoadStrategy

| Name | Type | Default |
| -- | -- | -- |
| `pageLoadStrategy` | `string` | `normal` |

## General

### automationName

| Name | Type | Default |
| -- | -- | -- |
| `appium:automationName` | `string` | Not specified |

Specifies the Appium driver to use. Must be set to `Espresso` (case-insensitive)

## Device

### deviceName

| Name | Type | Default |
| -- | -- | -- |
| `appium:deviceName` | `string` | Not specified |

The name of the device under test. Not used to select a device under test - use [`appium:udid`](#udid)
for real devices and [`appium:avd`](#avd) for emulators.

### platformVersion

| Name | Type | Default |
| -- | -- | -- |
| `appium:platformVersion` | `string` | Not specified |

The platform version of the device under test. Used for device selection if `appium:udid` or
`appium:avd` is not provided.

### udid

| Name | Type | Default |
| -- | -- | -- |
| `appium:udid` | `string` | Not specified |

UDID of the device under test. Can be retrieved by running `adb devices`. If neither this capability
nor `appium:avd` is set, the driver will automatically try to use the first connected device. Always
set this capability if you run parallel tests.

### disableSuppressAccessibilityService

| Name | Type | Default |
| -- | -- | -- |
| `appium:disableSuppressAccessibilityService` | `boolean` | `false` |

Whether the instrumentation process should avoid suppressing accessibility services during the
session. Useful if your automated test needs these services.

### clearDeviceLogsOnStart

| Name | Type | Default |
| -- | -- | -- |
| `appium:clearDeviceLogsOnStart` | `boolean` | `false` |

Whether device logs should be cleared upon session start (using `adb logcat -c`).

### ignoreHiddenApiPolicyError

| Name | Type | Default |
| -- | -- | -- |
| `appium:ignoreHiddenApiPolicyError` | `boolean` | `false` |

Whether to ignore failures caused by the driver automatically relaxing Android's hidden API access
policies, in order to enable access to non-SDK interfaces (such as logging). May be useful on
devices where access to these policies has been locked by its vendor.

### disableWindowAnimation

| Name | Type | Default |
| -- | -- | -- |
| `appium:disableWindowAnimation` | `boolean` | `true` |

Whether to disable window animations. [Google recommends disabling animations when running automated tests](https://developer.android.com/training/testing/espresso/setup#set-up-environment),
in order to avoid flakiness. The animation state is automatically restored after the session is
stopped, unless the session is ended unexpectedly.

### timeZone

| Name | Type | Default |
| -- | -- | -- |
| `appium:timeZone` | `string` | Not specified |

Value used to override the current timezone of the device. Persists until the next override. Must
be [a valid TZ identifier](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones).

Available since driver version 2.38.0.

### hideKeyboard

| Name | Type | Default |
| -- | -- | -- |
| `appium:hideKeyboard` | `boolean` | Not specified |

Whether to hide the on-screen keyboard during the session. This is achieved by [creating a custom "artificial" input method](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method).
It is recommended to use this feature only for special/exploratory cases, as it violates the way a
user normally interacts with the application under test.

If explicitly set to `false`, `adb shell ime reset` is run on session startup, which resets the
currently selected/enabled IMEs to the default ones, as if the device was initially booted with the
current locale.

### mockLocationApp

| Name | Type | Default |
| -- | -- | -- |
| `appium:mockLocationApp` | `string` | `io.appium.settings` |

Package identifier of the app to use for mocking device location. Has no effect on emulators. If
set to `null` or an empty string, Appium will skip the setup of the location mocking feature.

## Emulator (AVD)

### avd

| Name | Type | Default |
| -- | -- | -- |
| `appium:avd` | `string` | Not specified |

The name of Android emulator to run the test on. The names of currently installed emulators can
be listed by running `avdmanager list avd`. If the specified emulator is not running upon starting
a session, the driver will automatically launch it.

### avdLaunchTimeout

| Name | Type | Default |
| -- | -- | -- |
| `appium:avdLaunchTimeout` | `number` | `60000` |

Maximum number in milliseconds to wait until the Android emulator has started.

### avdReadyTimeout

| Name | Type | Default |
| -- | -- | -- |
| `appium:avdReadyTimeout` | `number` | `60000` |

Maximum number of milliseconds to wait until the Android emulator has fully booted and is ready for
usage.

### avdArgs

| Name | Type | Default |
| -- | -- | -- |
| `appium:avdArgs` | `string` or `Array<string>` | Not specified |

One or more [supported command-line arguments](https://developer.android.com/studio/run/emulator-commandline)
to apply when starting the emulator. Only applied if the emulator is not already running.

### avdEnv

| Name | Type | Default |
| -- | -- | -- |
| `appium:avdEnv` | `Record<string, any>` | Not specified |

One or more [environment variables](https://developer.android.com/tools/variables) to set when
starting the emulator. Only applied if the emulator is not already running.

### isHeadless

| Name | Type | Default |
| -- | -- | -- |
| `appium:isHeadless` | `boolean` | `false` |

Whether to start the emulator in headless mode. Equivalent to the [`-no-window` command-line argument](https://developer.android.com/studio/run/emulator-commandline).
Only applied if the emulator is not already running.

### networkSpeed

| Name | Type | Default |
| -- | -- | -- |
| `appium:networkSpeed` | `string` | Not specified |

The network speed to apply to the emulator. Equivalent to the [`-netspeed` command-line argument](https://developer.android.com/studio/run/emulator-commandline).
Only applied if the emulator is not already running.

### injectedImageProperties

| Name | Type | Default |
| -- | -- | -- |
| `appium:injectedImageProperties` | `Record<string, Record<string, number>>` | Not specified |

Adjusts properties of the image injected using the `mobile: injectEmulatorCameraImage` extension.
If the emulator is already running, it will be restarted in order to apply the properties.

The value of this capability is an object with the following keys. All keys and sub-keys are
optional, with the given defaults used for absent keys.

| Key | Description | Default |
| -- | -- | -- |
| `size` | Scale multipliers for X and Y axes | `{scaleX: 1, scaleY: 1}` |
| `position` | Offset coefficients for X/Y/Z axes, where 0 means centered | `{x: 0, y: 0, z: -1.5}` |
| `rotation` | Degrees of rotation for X/Y/Z axes | `{x: 0, y: 0, z: 0}` |

Available since driver version 2.43.0.

## ADB

### adbPort

### remoteAdbHost

### adbExecTimeout

### buildToolsVersion

### skipLogcatCapture

### suppressKillServer

### logcatFormat

### logcatFilterSpecs

### allowDelayAdb

### adbListenAllNetwork


## Espresso Server

### systemPort

### espressoBuildConfig

### espressoServerLaunchTimeout

### skipServerInstallation

### forceEspressoRebuild

### showGradleLog


## App Management

### app

### appPackage

### appActivity

### appWaitPackage

### appWaitActivity

### appWaitDuration

### activityOptions

### intentOptions

### androidInstallTimeout

### enforceAppInstall

### noReset

### fullReset

### autoGrantPermissions

### otherApps

### uninstallOtherPackages

### allowTestPackages

### remoteAppsCacheLimit


## App Signing

### useKeystore

### keystorePath

### keystorePassword

### keyAlias

### keyPassword

### noSign


## App Localization

### localeScript

### language

### locale

### appLocale


## Device Lock

### skipUnlock

### unlockType

### unlockKey

### unlockSuccessTimeout


## Web Context

### autoWebview

### autoWebviewTimeout

### webviewDevtoolsPort

### ensureWebviewsHavePages

### enableWebviewDetailsCollection

### chromeOptions

### chromedriverPort

### chromedriverPorts

### chromedriverArgs

### chromedriverExecutable

### chromedriverExecutableDir

### chromedriverChromeMappingFile

### chromedriverUseSystemExecutable

### chromedriverDisableBuildCheck

### recreateChromeDriverSessions

### nativeWebScreenshot

### extractChromeAndroidPackageFromContextName

### showChromedriverLog
