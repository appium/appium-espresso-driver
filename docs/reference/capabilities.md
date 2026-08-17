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

### avdLaunchTimeout

### avdReadyTimeout

### avdArgs

### avdEnv

### isHeadless

### networkSpeed

### gpsEnabled

### injectedImageProperties


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
