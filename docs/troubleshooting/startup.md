---
title: Session Startup Issues
---

Issues during session startup usually have the following causes:

* Mismatched dependencies between the Espresso server and AUT
* Problems when launching the AUT package/activity

## Startup Crashes or Exceptions

If the Espresso server crashes on startup, or the logcat output returns various exceptions about
missing classes/methods, it is possible that the [`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig)
capability may not be configured correctly. Finding the correct configuration might require some
experimentation, since different apps have different module requirements. Refer to
[this issue](https://github.com/appium/appium-espresso-driver/issues/812) for additional guidance.

Another solution might be to integrate the Espresso server with the application under test
[as a library](../guides/usage-as-library.md).

## Resources Not Found

In case of session startup failures due to exceptions similar to `Resources$NotFoundException`, it
may help to adjust ProGuard rules in the app under test:

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

Refer to [this issue comment](https://github.com/appium/appium-espresso-driver/issues/449#issuecomment-537833139)
for more details on this topic.

## Signing

Espresso requires the debug APK and app-under-test APK (AUT) to have the same signature. The driver
automatically signs the AUT with the `io.appium.espressoserver.test` signature. This may be
problematic if using an outdated Android SDK tools and/or an outdated Java version.

## Outdated Espresso Server

If there are problems starting a session, set the [`appium:forceEspressoRebuild`](../reference/capabilities.md#forceespressorebuild)
capability to `true` and retry. This will force the Espresso server app to be rebuilt. If the
following session startup is successful, set it back to `false`, so the session startup performance
is back to normal.

## Permission Denial When Starting Intent

The full error description usually looks like `'java.lang.SecurityException: Permission Denial: starting Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.mypackage/.myactivity.MainActivity launchParam=MultiScreenLaunchParams { mDisplayId=0 mBaseDisplayId=0 mFlags=0 } } from null (pid=11366, uid=2000) not exported from uid 10191`.
An error like this might indicate that the provided application package and activity name (either
passed to Espresso as [`appium:appPackage`](../reference/capabilities.md#apppackage) /
[`appium:appActivity`](../reference/capabilities.md#appactivity) or auto detected), is not the
correct one to start the application under test.

In order to fix this, it is necessary to check the correct values with the application developer,
and test them manually first by executing: `adb shell am start -W -n com.myfixedpackage/.myfixedactivity.MainActivity -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -f 0x10200000`.
If this commands succeeds manually and starts the necessary application on the device, then it will
work for the Espresso driver as well.

## Activity Never Started

This problem may manifest with a message like `com.myactivity.* never started`. It usually
indicates that the _first_ application activity (either passed to Espresso as
[`appium:appWaitPackage`](../reference/capabilities.md#appwaitpackage) /
[`appium:appWaitActivity`](../reference/capabilities.md#appwaitactivity) or auto-detected) is not
one actually launched as the first one. This problem typically occurs in applications with multiple
activities.

In order to resolve the problem, one should check with application developer regarding which
activity/package is the very first one that appears on application startup. The currently focused
activity name can be verified using the `adb shell dumpsys window windows` command (see the
[Activity Startup guide](../guides/activity-startup.md)).

The Espresso driver also allows the use of wildcards in the `appium:appWaitActivity` value, which
can be particularly useful if the activity name is generated dynamically, or it is not the same all
the time. For example, `com.mycompany.*` will match any of `com.mycompany.foo`, `com.mycompany.bar`.

## Command Timed Out

If the activity names are correct, but the startup still times out, another possible approach is to
increase the value of the [`app:appWaitDuration`](../reference/capabilities.md#appwaitduration)
capability. Normally, the default 20 seconds is enough for the most of applications, however, some
bigger apps might require more time to start and show the first activity. (It is strongly
recommended to not develop apps to behave this way.)

You may also consider setting the [`appium:autoLaunch`](../reference/capabilities.md#autolaunch)
capability to false, which skips waiting for the activity. However, by choosing this option, the
driver cannot make sure the activity has fully started, so then it is up to the client code to
verify the initial UI state is the one that is expected.
