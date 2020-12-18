# appium-espresso-driver

[![Build Status](https://dev.azure.com/AppiumCI/Appium%20CI/_apis/build/status/appium.appium-espresso-driver?branchName=master)](https://dev.azure.com/AppiumCI/Appium%20CI/_build/latest?definitionId=3&branchName=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a877b7395f2d475aa79c08daf665dc3c)](https://www.codacy.com/app/dpgraham/appium-espresso-driver?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=appium/appium-espresso-driver&amp;utm_campaign=Badge_Grade)

Appium's Espresso Driver is a test automation server for Android that uses [Espresso](https://developer.android.com/training/testing/espresso/) as the underlying test technology. The Espresso Driver is a part of the Appium framework. The driver operates in scope of [W3C WebDriver protocol](https://www.w3.org/TR/webdriver/) with several custom extensions to cover operating-system specific scenarios.

The Espresso package consists of two main parts:
- The driver part (written in Node.js) ensures the communication between the Espresso server and Appium. Also includes several handlers that directly use ADB and/or other system tools without a need to talk to the server.
- The server part (written in Kotlin with some parts of Java), which is running on the device under test and transforms REST API calls into low-level Espresso commands.


## Comparison with UiAutomator2

The key difference between [UiAutomator2 Driver](https://github.com/appium/appium-uiautomator2-driver) and Espresso Driver is that UiAutomator2 is a black-box testing framework, and Espresso is a "grey-box" testing framework. The Espresso Driver itself is black-box (no internals of the code are exposed to the tester), but the Espresso framework itself has access to the internals of Android applications. This distinction has a few notable benefits. It can find elements that aren't rendered on the screen, it can identify elements by the Android View Tag and it makes use of [IdlingResource](https://developer.android.com/reference/android/support/test/espresso/IdlingResource) which blocks the framework from running commands until the UI thread is free. There is limited support to automate out of app areas using the mobile command [uiautomator](https://github.com/appium/appium-espresso-driver/blob/b2b0883ab088a131a47d88f6aeddd8ff5882087d/lib/commands/general.js#L188)


## Requirements

On top of standard Appium requirements Espresso driver also expects the following prerequisites:

- Windows, Linux and macOS are supported as hosts
- [Android SDK Platform tools](https://developer.android.com/studio/releases/platform-tools) must be installed. [Android Studio IDE](https://developer.android.com/studio) also provides a convenient UI to install and manage the tools.
- ANDROID_HOME or ANDROID_SDK_ROOT [environment variable](https://developer.android.com/studio/command-line/variables) must be set
- Java JDK must be installed and [JAVA_HOME](https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux) environment variable must be set. Android SDK below API 30 requires Java 8. Android SDK 30 and above requires Java 9 or newer.
- [Emulator](https://developer.android.com/studio/run/managing-avds) platform image must be installed if you plan to run your tests on it. [Android Studio IDE](https://developer.android.com/studio) also provides a convenient UI to install and manage emulators.
- Real Android devices must have [USB debugging enabled](https://developer.android.com/studio/debug/dev-options) and should be visible as `online` in `adb devices -l` output.
- The minimum version of Android API must be 5.0 (API level 21) (6.0 is recommended as version 5 has some known compatibility issues).
- [Gradle](https://gradle.org/) must be installed in order to build Espresso server.
- Both the server package and the application under test must be signed with the same digital signature. Appium does sign them automatically upon session creation, so this could only be an issue if one wants to test an application, which is already installed on the device (using `noReset=true` capability).
- The package under test must not have mangled class names (e.g. [Proguard](https://developer.android.com/studio/build/shrink-code) must not be enabled for it)


## Capabilities

### General

Capability Name | Description
--- | ---
platformName | Could be set to `android`. Appium itself is not strict about this capability value if `automationName` is provided, so feel free to assign it to any supported platform name if this is needed, for example, to make Selenium Grid working.
appium:automationName | Must always be set to `espresso`. Values of `automationName` are compared case-insensitively.
appium:deviceName | The name of the device under test (actually, it is not used to select a device under test). Consider setting `udid` for real devices and `avd` for emulators instead
appium:platformVersion | The platform version of an emulator or a real device. This capability is used for device autodetection if `udid` is not provided
appium:udid | UDID of the device to be tested. Could ve retrieved from `adb devices -l` output. If unset then the driver will try to use the first connected device. Always set this capability if you run parallel tests.
appium:noReset | Prevents the device to be reset before the session startup if set to `true`. This means that the application under test is not going to be terminated neither its data cleaned. `false` by default
appium:fullReset | Being set to `true` always enforces the application under test to be fully uninstalled before starting a new session. `false` by default
appium:printPageSourceOnFindFailure | Enforces the server to dump the actual XML page source into the log if any error happens. `false` by default.

### Driver/Server

Capability Name | Description
--- | ---
appium:systemPort | The number of the port the Espresso server is listening on. By default the first free port from 8300..8399 range is selected. It is recommended to set this value if you are running [parallel tests](https://github.com/appium/appium/blob/master/docs/en/advanced-concepts/parallel-tests.md) on the same machine.
appium:skipServerInstallation | Skip the Espresso Server component installation on the device under test and all the related checks if set to `true`. This could help to speed up the session startup if you know for sure the correct server version is installed on the device. In case the server is not installed or an incorrect version of it is installed then you may get an unexpected error later. `false` by default
appium:espressoServerLaunchTimeout | The maximum number of milliseconds to wait util Espresso server is listening on the device. `45000` ms by default
appium:forceEspressoRebuild | Whether to always enforce Espresso server rebuild (`true`). By default Espresso caches the already built server apk and only rebuilds it when it is necessary, because rebuilding process needs extra time. `false` by default
appium:espressoBuildConfig | Either the full path to build config JSON on the server file system or the JSON content itself serialized to a string. This config allows to customize several important properties of Espresso server. Refer to TBD for more information on how to properly construct such config.
appium:showGradleLog | Whether to include Gradle log to the regular server logs while building Espresso server. `false` by default.

### App

Capability Name | Description
--- | ---
appium:app | Full path to the application to be tested (the app must be located on the same machine where the server is running). Both `.apk` and `.apks` application extensions are supported. Could also be an URL to a remote location. If neither of the `app` or `appPackage` capabilities are provided then the driver will fail to start a session. Also, if `app` capability is not provided it is expected that the app under test is already installed on the device under test and `noReset` is equal to `true`.
appium:appPackage | Application package identifier to be started. If not provided then Espresso will try to detect it automatically from the package provided by the `app` capability. Read [How To Troubleshoot Activities Startup](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/android/activity-startup.md) for more details
appium:appActivity | Main application activity identifier. If not provided then Espresso will try to detect it automatically from the package provided by the `app` capability. Read [How To Troubleshoot Activities Startup](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/android/activity-startup.md) for more details
appium:appWaitActivity | Identifier of the first activity that the application invokes. If not provided then equals to `appium:appActivity`. Read [How To Troubleshoot Activities Startup](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/android/activity-startup.md) for more details
appium:appWaitPackage | Identifier of the first package that is invoked first. If not provided then equals to `appium:appPackage`. Read [How To Troubleshoot Activities Startup](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/android/activity-startup.md) for more details
appium:appWaitDuration | Maximum amount of milliseconds to wait until the application under test is started (e. g. an activity returns the control to the caller). `20000` ms by default. Read [How To Troubleshoot Activities Startup](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/android/activity-startup.md) for more details
appium:intentOptions | The mapping of custom options for the intent that is going to be started. Read TBD for more details.
appium:activityOptions | The mapping of custom options for the activity that is going to be started. Read TBD for more details.
appium:androidInstallTimeout | Maximum amount of milliseconds to wait until the application under test is installed. `90000` ms by default
appium:autoGrantPermissions | Whether to grant all the requested application permissions automatically when a test starts(`true`). `false` by default
appium:otherApps | Allows to set one or more comma-separated paths to Android packages that are going to be installed along with the main application under test. This might be useful if the tested app has dependencies
appium:uninstallOtherPackages | Allows to set one or more comma-separated package identifiers to be uninstalled from the device before a test starts
appium:allowTestPackages | If set to `true` then it would be possible to use packages built with the test flag for the automated testing (literally adds `-t` flag to the `adb install` command). `false` by default
appium:remoteAppsCacheLimit | Sets the maximum amount of application packages to be cached on the device under test. This is needed for devices that don't support streamed installs (Android 7 and below), because ADB must push app packages to the device first in order to install them, which takes some time. Setting this capability to zero disables apps caching. `10` by default.
appium:enforceAppInstall | If set to `true` then the application under test is always reinstalled even if a newer version of it already exists on the device under test. `false` by default

### App Localization

Capability Name | Description
--- | ---
appium:localeScript | Canonical name of the locale to be set for the app under test, for example `zh-Hans-CN`. See https://developer.android.com/reference/java/util/Locale.html for more details.
appium:language | Name of the language to extract application strings for. Strings are extracted for the current system language by default. Also sets the language for the app under test. See https://developer.android.com/reference/java/util/Locale.html for more details. Example: en, ja
appium:locale | Sets the locale for the app under test. See https://developer.android.com/reference/java/util/Locale.html for more details. Example: EN, JA
appium:appLocale | Sets the locale for the app under test. The main difference between this option and the above ones is that this option only changes the locale for the application under test and does not affect other parts of the system. Also, it only uses public APIs for its purpose. See https://github.com/libyal/libfwnt/wiki/Language-Code-identifiers to get the list of available language abbreviations. Example: `{"language": "zh", "country": "CN", "variant": "Hans"}`

### ADB

Capability Name | Description
--- | ---
appium:adbPort | Number of the port where ADB is running. `5037` by default
appium:remoteAdbHost | Address of the host where ADB is running (the value of `-H` ADB command line option). Unset by default
appium:adbExecTimeout | Maximum number of milliseconds to wait until single ADB command is executed. `20000` ms by default
appium:clearDeviceLogsOnStart | If set to `true` then Espresso deletes all the existing logs in the device buffer before starting a new test
appium:buildToolsVersion | The version of Android build tools to use. By default Espresso driver uses the most recent version of build tools installed on the machine, but sometimes it might be necessary to give it a hint (let say if there is a known bug in the most recent tools version). Example: `28.0.3`
appium:skipLogcatCapture | Being set to `true` disables automatic logcat output collection during the test run. `false` by default
appium:suppressKillServer | Being set to `true` prevents the driver from ever killing the ADB server explicitly. Could be useful if ADB is connected wirelessly. `false` by default
appium:ignoreHiddenApiPolicyError | Being set to `true` ignores a failure while changing hidden API access policies. Could be useful on some devices, where access to these policies has been locked by its vendor. `false` by default.
appium:mockLocationApp | If set to `true` then location mocking app gets assigned to Appium Settings (the default behavior), so Appium could mock GPS location. The `false` value prevents that from happening.
appium:logcatFormat | The log print format, where `format` is one of: `brief` `process` `tag` `thread` `raw` `time` `threadtime` `long`. `threadtime` is the default value.
appium:logcatFilterSpecs | Series of `tag[:priority]` where `tag` is a log component tag (or * for all) and priority is: `V    Verbose`, `D    Debug`, `I    Info`, `W    Warn`, `E    Error`, `F    Fatal`, `S    Silent (supress all output)`. '*' means '*:d' and `tag` by itself means `tag:v`. If not specified on the commandline, filterspec is set from ANDROID_LOG_TAGS. If no filterspec is found, filter defaults to '*:I'.
appium:allowDelayAdb | Being set to `false` prevents emulator to use `-delay-adb` feature to detect its startup. See https://github.com/appium/appium/issues/14773 for more details.

### Emulator (Android Virtual Device)

Capability Name | Description
--- | ---
appium:avd | The name of Android emulator to run the test on. The names of currently installed emulators could be listed using `avdmanager list avd` command. If the emulator with the given name is not running then it is going to be started before a test
appium:avdLaunchTimeout | Maximum number of milliseconds to wait until Android Emulator is started. `60000` ms by default
appium:avdReadyTimeout | Maximum number of milliseconds to wait until Android Emulator is fully booted and is ready for usage. `60000` ms by default
appium:avdArgs | Either a string or an array of emulator [command line arguments](https://developer.android.com/studio/run/emulator-commandline).
appium:avdEnv | Mapping of emulator [environment variables](https://developer.android.com/studio/command-line/variables).
appium:networkSpeed | Sets the desired network speed limit for the emulator. It is only applied if the emulator is not running before the test starts. See emulator [command line arguments](https://developer.android.com/studio/run/emulator-commandline) description for more details.
appium:gpsEnabled | Sets whether to enable (`true`) or disable (`false`) GPS service in the Emulator. Unset by default, which means to not change the current value
appium:isHeadless | If set to `true` then emulator starts in headless mode (e.g. no UI is shown). It is only applied if the emulator is not running before the test starts. `false` by default.

### App Signing

Capability Name | Description
--- | ---
appium:useKeystore | Whether to use a custom keystore to sign the app under test. `false` by default, which means apps are always signed with the default Appium debug certificate (unless canceled by `noSign` capability). This capability is used in combination with `keystorePath`, `keystorePassword`, `keyAlias` and `keyPassword` capabilities.
appium:keystorePath | The full path to the keystore file on the server filesystem. This capability is used in combination with `useKeystore`, `keystorePath`, `keystorePassword`, `keyAlias` and `keyPassword` capabilities. Unset by default
appium:keystorePassword | The password to the keystore file provided in `keystorePath` capability. This capability is used in combination with `useKeystore`, `keystorePath`, `keystorePassword`, `keyAlias` and `keyPassword` capabilities. Unset by default
appium:keyAlias | The alias of the key in the keystore file provided in `keystorePath` capability. This capability is used in combination with `useKeystore`, `keystorePath`, `keystorePassword`, `keyAlias` and `keyPassword` capabilities. Unset by default
appium:keyPassword | The password of the key in the keystore file provided in `keystorePath` capability. This capability is used in combination with `useKeystore`, `keystorePath`, `keystorePassword`, `keyAlias` and `keyPassword` capabilities. Unset by default
appium:noSign | Set it to `true` in order to skip application signing. By default all apps are always signed with the default Appium debug signature. This capability cancels all the signing checks and makes the driver to use the application package as is. This capability does not affect `.apks` packages as these are expected to be already signed. Make sure that the server package is signed with the same signature as the application under test before disabling this capability.

### Device Locking

Capability Name | Description
--- | ---
appium:skipUnlock | Whether to skip the check for lock screen presence (`true`). By default Espresso driver tries to detect if the device's screen is locked before starting the test and to unlock that (which sometimes might be unstable). Note, that this operation takes some time, so it is highly recommended to set this capability to `false` and disable screen locking on devices under test.
appium:unlockType | Set one of the possible types of Android lock screens to unlock. Read the [Unlock tutorial](https://github.com/appium/appium-android-driver/blob/master/docs/UNLOCK.md) for more details.
appium:unlockKey | Allows to set an unlock key. Read the [Unlock tutorial](https://github.com/appium/appium-android-driver/blob/master/docs/UNLOCK.md) for more details.
appium:unlockSuccessTimeout | Maximum number of milliseconds to wait until the device is unlocked. `2000` ms by default

### Web Context

Capability Name | Description
--- | ---
appium:autoWebview | If set to `true` then Espresso driver will try to switch to the first available web view after the session is started. `false` by default.
appium:webviewDevtoolsPort | The local port number to use for devtools communication. By default the first free port from 10900..11000 range is selected. Consider setting the custom value if you are running parallel tests.
appium:ensureWebviewsHavePages | Whether to skip web views that have no pages from being shown in `getContexts` output. The driver uses devtools connection to retrieve the information about existing pages. `true` by default.
appium:enableWebviewDetailsCollection | Whether to retrieve extended web views information using devtools protocol. Enabling this capability helps to detect the necessary chromedriver version more precisely. `false` by default.
appium:chromedriverPort | The port number to use for Chromedriver communication. Any free port number is selected by default if unset.
appium:chromedriverPorts | Array of possible port numbers to assign for Chromedriver communication. If none of the port in this array is free then an error is thrown.
appium:chromedriverArgs | Array of chromedriver [command line arguments](http://www.assertselenium.com/java/list-of-chrome-driver-command-line-arguments/). Note, that not all command line arguments that are available for the desktop browser are also available for the mobile one.
appium:chromedriverExecutable | Full path to the chromedriver executable on the server file system.
appium:chromedriverExecutableDir | Full path to the folder where chromedriver executables are located. This folder is used then to store the downloaded chromedriver executables if automatic download is enabled. Read [Automatic Chromedriver Discovery article](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/web/chromedriver.md#automatic-discovery-of-compatible-chromedriver) for more details.
appium:chromedriverChromeMappingFile | Full path to the chromedrivers mapping file. This file is used to statically map webview/browser versions to the chromedriver versions that are capable of automating them. Read [Automatic Chromedriver Discovery article](https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/web/chromedriver.md#automatic-discovery-of-compatible-chromedriver) for more details.
appium:chromedriverUseSystemExecutable | Set it to `true` in order to enforce the usage of chromedriver, which gets downloaded by Appium automatically upon installation. This driver might not be compatible with the destination browser or a web view. `false` by default.
appium:chromedriverDisableBuildCheck | Being set to `true` disables the compatibility validation between the current chromedriver and the destination browser/web view. Use it with care.
appium:autoWebviewTimeout | Set the maximum number of milliseconds to wait until a web view is available if `autoWebview` capability is set to `true`. `2000` ms by default
appium:recreateChromeDriverSessions | If this capability is set to `true` then chromedriver session is always going to be killed and then recreated instead of just suspending it on context switching. `false` by default
appium:nativeWebScreenshot | Whether to use screenshoting endpoint provided by Espresso framework (`true`) rather than the one provided by chromedriver (`false`, the default value). Use it when you experience issues with the latter.
appium:extractChromeAndroidPackageFromContextName | If set to `true`, tell chromedriver to attach to the android package we have associated with the context name, rather than the package of the application under test. `false` by default.
appium:showChromedriverLog | If set to `true` then all the output from chromedriver binary will be forwarded to the Appium server log. `false` by default.
pageLoadStrategy | One of the available page load strategies. See https://www.w3.org/TR/webdriver/#capabilities
appium:chromeOptions | A mapping, that allows to customize chromedriver options. See https://chromedriver.chromium.org/capabilities for the list of available entries.

### Other

Capability Name | Description
--- | ---
appium:disableSuppressAccessibilityService | Being set to `true` tells the instrumentation process to not suppress accessibility services during the automated test. This might be useful if your automated test needs these services. `false` by default


## Element Attributes

Espresso driver supports the following element attributes:

Name | Description | Example
--- | --- | ---
checkable | Whether the element is checkable or not | 'true'
checked | Whether the element is checked. Always `false` if the element is not checkable | 'false'
class | The full name of the element's class. Could be `null` for some elements | 'android.view.View'
clickable | Whether the element could be clicked | 'false'
content-desc | The content-description attribute of the accessible element | 'foo'
enabled | Whether the element could be clicked | 'true'
focusable | Whether the element could be focused | 'true'
focused | Whether the element could is focused. Always `false` if the element is not focusable | 'false'
long-clickable | Whether the element accepts long clicks | 'false'
package | Identifier of the package the element belongs to | 'com.mycompany'
password | Whether the element is a password input field | 'true'
resource-id | Element's resource identifier. Could be `null` | 'com.mycompany:id/resId'
scrollable | Whether the element is scrollable | 'true'
selected | Whether the element is selected | 'false'
text | The element's text. It never equals to `null` | 'my text'
hint | The element's hint. Could be `null` | 'my hint text'
bounds | The element's visible frame (`[left, top][right, bottom]`) | `[0,0][100,100]`
adapters | Comma-separated list of all adapter data items if the element's view hierarchy contains any, otherwise `null` | TBD
adapter-type | The full class name of the adapter data items if the element's view hierarchy contains any, otherwise `null` | TBD
no-multiline-buttons | Whether the element's view hierarchy does not contain multiline buttons | 'true'
no-overlaps | Whether element's descendant objects assignable to TextView or ImageView do not overlap each other | 'true'
no-ellipsized-text | Whether the element's view hierarchy does not contain ellipsized or cut off text views | 'false'
visible | Whether the element is visible to the user | 'true'
view-tag | The tag value assigned to the element. Could be `null` | 'my tag'


## Element Location

Espresso driver supports the following location strategies:

Name | Description | Example
--- | --- | ---
id | This strategy is mapped to the native Espresso `withId` [matcher](https://developer.android.com/reference/androidx/test/espresso/matcher/ViewMatchers#withId(org.hamcrest.Matcher%3Cjava.lang.Integer%3E)) (exact match of element's resource id). Package identifier prefix is added automatically if unset and is equal to the identifier of the current application under test. | 'com.mycompany:id/resourceId'
accessibility id | This strategy is mapped to the native Espresso `withContentDescription` [matcher](https://developer.android.com/reference/androidx/test/espresso/matcher/ViewMatchers#withcontentdescription_1) (exact match of element's content description). | 'my description'
class name | This strategy is mapped to the native Espresso `withClassName` [matcher](https://developer.android.com/reference/androidx/test/espresso/matcher/ViewMatchers#withClassName(org.hamcrest.Matcher%3Cjava.lang.String%3E)) (exact match of element's class name). | 'android.view.View'
text | This strategy is mapped to the native Espresso `withText` [matcher](https://developer.android.com/reference/androidx/test/espresso/matcher/ViewMatchers#withText(org.hamcrest.Matcher%3Cjava.lang.String%3E)) (exact match of element's text). | 'my text'
`-android viewtag` or `tag name` | This strategy is mapped to the native Espresso `withTagValue` [matcher](https://developer.android.com/reference/androidx/test/espresso/matcher/ViewMatchers#withtagvalue) (exact match of element's tag value). | 'my tag'
-android datamatcher | TBD
-android viewmatcher | TBD
xpath | For elements lookup Xpath strategy the driver uses the same XML tree that is generated by page source API. Only Xpath 1.0 is supported. | `By.xpath("//android.view.View[@text=\"Regular\" and @checkable=\"true\"]")`


## Troubleshooting

* If there are ever problems starting a session, try setting the capability `forceEspressoRebuild=true` and retrying. This will rebuild a fresh Espresso Server APK. If the session is succcesful, set it back to false so that it doesn't re-install on every single test.
* Espresso requires the debug APK and app-under-test APK (AUT) to have the same signature. It automatically signs the AUT with the `io.appium.espressoserver.test` signature. This may have problems if you're using an outdated Android SDK tools and/or an outdated Java version.
* If you experience session startup failures due to exceptions similar to `Resources$NotFoundException` then try to adjust your ProGuard rules:
  ```
  -dontwarn com.google.android.material.**
  -keep class com.google.android.material.** { *; }

  -dontwarn androidx.**
  -keep class androidx.** { *; }
  -keep interface androidx.** { *; }

  -dontwarn android.support.v4.**
  -keep class android.support.v4.** { *; }

  -dontwarn android.support.v7.**
  -keep class android.support.v7.** { *; }
  ```
  Please read [#449](https://github.com/appium/appium-espresso-driver/issues/449#issuecomment-537833139) for more details on this topic.

## Contributing

### Contents of Repo

* `espresso-server/`: Android Java code that gets built into a test apk. The test apk runs a NanoHTTP server that implements the WebDriver protocol.
* `lib/`: NodeJS code that constitutes the Appium driver, which is responsible for handling capabilities and starting up the Espresso instrumentation context. Once the Espresso server is up, this code is responsible for proxying user requests to it.

### Running

* To build the Espresso server _and_ the NodeJS code, run `npm run build`
* To just build the Espresso server, run `npm run build:server` or `cd espresso-server && ./gradlew clean assembleDebug assembleAndroidTest`. The server can also be built from Android Studio.
* To just build NodeJS code, run `gulp transpile`


### Tests

* Espresso server unit tests are located at `io.appium.espressoserver.test` and can be run in Android Studio
* NodeJS unit tests are run with `npm run test`
* End-to-end tests are run with `npm run e2e-test` (remember to run `npm run build` before running this command so that it has up-to-date Espresso Server and NodeJS code)
