---
title: System Requirements
---

There are five primary requirements to use the Espresso driver:

* macOS, Windows or Linux host machine
* Android SDK
    * Android SDK Platform & Build-Tools (via [Android Studio](https://developer.android.com/studio) or [standalone](https://developer.android.com/tools/sdkmanager))
    * Android SDK Platform-Tools (via [Android Studio](https://developer.android.com/studio) or [standalone](https://developer.android.com/tools/releases/platform-tools))
    * [`ANDROID_HOME` environment variable must be set](https://developer.android.com/tools/variables)
* Java Development Kit (JDK)
    * Can be downloaded from hosts like [Oracle](https://jdk.java.net/) or [Adoptium](https://adoptium.net/en-GB/temurin/releases/)
    * [`JAVA_HOME` environment variable must be set](https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux)
* [Gradle](https://gradle.org/)
* Appium

For most use cases, the latest versions of the above prerequisites should work just fine. Support for
older versions is specified in the tables listed below.

!!! note

    This document only lists compatibility information starting from Espresso driver 2.0.0, which
    was the first version supporting Appium 2. For compatibility with driver versions older than 2.0.0
    (Appium 1), please refer to [the Appium 1 changelog](https://github.com/appium/appium/blob/1.x/CHANGELOG.md).

!!! note

    If you already have the driver installed, since version 2.31.0 you can also verify most of its
    requirements with the built-in Appium Doctor:

    ```
    appium driver doctor espresso
    ```

## Android OS

The Espresso driver can only automate devices under test that are running at least its minimum
supported Android OS version.

Please note that this OS version is not the same as the Android SDK Platform version listed in the
aforementioned host machine requirements.

| Espresso driver version | Minimum Android OS version on the device under test |
| --- | --- |
| >= 6.0.0 | Android 8 (Oreo / API level 26) |
| 2.0.0 - 5.0.4 | Android 5 (Lollipop / API level 21) [^android5] |

## JDK & Gradle

Gradle is required to build the Espresso server application. JDK is required both by Gradle and the
Android SDK.

| Espresso driver version | Minimum JDK version | Minimum Gradle version |
| --- | --- | --- |
| >= 8.0.0 | JDK 17 [^jdk17] | 7.3 |
| 2.1.0 - 7.2.1 | JDK 11 | 5.0 |

## Appium Server

Make sure to install a version of Appium that supports your target driver version. The requirements
and prerequisites of Appium itself can be found in [the Appium documentation](https://appium.io/docs/en/latest/quickstart/install/).

| Espresso driver version | Supported Appium server version |
| --- | --- |
| >= 5.0.0 | Appium 3 |
| 2.0.0 - 4.1.17 | Appium 2 |

[^android5]: Partial support due to known compatibility issues. Android 6 (Marshmallow / API level 23) or later is recommended.
[^jdk17]: Driver versions 8.0.0 - 8.1.0 support _only_ JDK 17. Driver versions 8.2.0 and later also support newer JDKs.
