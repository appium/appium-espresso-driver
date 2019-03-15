# appium-espresso-driver

[![Build Status](https://dev.azure.com/AppiumCI/Appium%20CI/_apis/build/status/appium.appium-espresso-driver?branchName=master)](https://dev.azure.com/AppiumCI/Appium%20CI/_build/latest?definitionId=3&branchName=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a877b7395f2d475aa79c08daf665dc3c)](https://www.codacy.com/app/dpgraham/appium-espresso-driver?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=appium/appium-espresso-driver&amp;utm_campaign=Badge_Grade)
[![Greenkeeper badge](https://badges.greenkeeper.io/appium/appium-espresso-driver.svg)](https://greenkeeper.io/)

Appium's Espresso Driver is a test automation server for Android that uses [Espresso](https://developer.android.com/training/testing/espresso/) as the underlying test technology. The Espresso Driver is a part of the Appium framework.

## Comparison with UiAutomator2

The key difference between [UiAutomator2 Driver](https://github.com/appium/appium-uiautomator2-driver) and Espresso Driver is that UiAutomator2 is a black-box testing framework, and Espresso is a "grey-box" testing framework. The Espresso Driver itself is black-box (no internals of the code are exposed to the tester), but the Espresso framework itself has access to the internals of Android applications. This distinction has a few notable benefits. It can find elements that aren't rendered on the screen, it can identify elements by the Android View Tag and it makes use of [IdlingResource](https://developer.android.com/reference/android/support/test/espresso/IdlingResource) which blocks the framework from running commands until the UI thread is free. There is limited support to automate out of app areas using the mobile command [uiautomator](https://github.com/appium/appium-espresso-driver/blob/b2b0883ab088a131a47d88f6aeddd8ff5882087d/lib/commands/general.js#L188)

## Troubleshooting

* If there are ever problems starting a session, try setting the capability `forceEspressoRebuild=true` and retrying. This will rebuild a fresh Espresso Server APK. If the session is succcesful, set it back to false so that it doesn't re-install on every single test.
* Espresso requires the debug APK and app-under-test APK (AUT) to have the same signature. It automatically signs the AUT with the `io.appium.espressoserver.test` signature. This may have problems if you're using an outdated Android SDK tools and/or an outdated Java version.

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
