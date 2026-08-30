---
title: Activity Startup
---

> The `Activity` class is a crucial component of an Android app, and the way activities are launched and put together is a fundamental part of the platform's application model. Unlike programming paradigms in which apps are launched with a `main` method, the Android system initiates code in an `Activity` instance by invoking specific callback methods that correspond to specific stages of its lifecycle.
>
> &copy; [Android Developer Documentation](https://developer.android.com/guide/components/activities/intro-activities)

The Espresso driver needs to know package and activity names in order to properly initialize the
application under test. This information is expected to be provided in session capabilities.

## Startup Capabilities

The driver supports multiple activity startup-related capabilities:

- [`appium:appActivity`](../reference/capabilities.md#appactivity): main application activity name
- [`appium:appPackage`](../reference/capabilities.md#apppackage): application package ID
- [`appium:appWaitActivity`](../reference/capabilities.md#appwaitactivity): application activity
  name to wait for/which starts the first
- [`appium:appWaitPackage`](../reference/capabilities.md#appwaitpackage): application package ID to
  wait for/which starts the first
- [`appium:appWaitDuration`](../reference/capabilities.md#appwaitduration): maximum duration to wait
  until `appWaitActivity` is focused

All of these capabilities are optional. If they are not set explicitly, the driver tries to
auto-detect them by reading their values from the manifest of the APK file specified using the
[`appium:app`](../reference/capabilities.md#app) capability.

If the application under test is supposed to be already installed on the device, then at least the
`appium:appActivity` and `appium:appPackage` capabilities must be set, since no package manifest is
available in such case.

For more details, check the implementation of the `packageAndLaunchActivityFromManifest` method in
the [`appium-adb`](https://github.com/appium/appium-adb/blob/master/lib/tools/android-manifest.js)
package.

## How Espresso Starts Activities

1. Activities are started by [Android Instrumentation](https://developer.android.com/guide/components/activities/testing)
2. Espresso tries to start the specified `appium:appPackage`/`appium:appActivity` combination
3. Espresso waits until the `appWaitPackage`/`appWaitActivity` is focused (or the `appWaitDuration`
   timeout expires)
4. The currently focused activity name is parsed from `adb shell dumpsys window windows` command
   output (`mFocusedApp` or `mCurrentFocus` entries)

For more details, check the implementation of the `getFocusedPackageAndActivity` method in the
[`appium-adb`](https://github.com/appium/appium-adb/blob/master/lib/tools/apk-utils.js) package,
as well as the [`startActivity`](https://github.com/appium/appium-espresso-driver/blob/master/espresso-server/app/src/androidTest/java/io/appium/espressoserver/lib/helpers/ActivityHelpers.kt)
helper inside the Espresso server code.

## Troubleshooting

Refer to the [Session Startup Issues](../troubleshooting/startup.md) guide for handling potential
issues.
