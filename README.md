# appium-espresso-driver

Espresso integration for Appium

Note: this is an experimental work in progress and is not ready for use.

## Developers

### Building Espresso Server

```
cd espresso-server
./gradlew clean assembleDebug assembleAndroidTest
```

### Layout

* `espresso-server`: Java code that gets built into a test apk, which contains the WebDriver-based server to be run in the Espresso context.
* `lib`: Node.js code that constitutes the Appium driver, which is responsible for handling capabilities and starting up the Espresso instrumentation context. Once the Espresso server is up, this code is responsible for proxying user requests to it.
