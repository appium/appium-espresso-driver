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


## Device

### deviceName

### platformVersion

### udid

### disableSuppressAccessibilityService

### clearDeviceLogsOnStart

### ignoreHiddenApiPolicyError

### disableWindowAnimation

### timeZone

### hideKeyboard

### mockLocationApp


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
