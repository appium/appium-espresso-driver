## Consuming Espresso Server as Library

After driver version 3.0.0 it is possible to consume Espresso server as a library,
apply dependency constraints to align dependency versions,
and embed it either as androidTest component of the app
or as a standalone test module under the same
Gradle project. The library only has to expose a single method for starting a
server and a TestRule for Compose support.
This way, the Espresso server APK could be built with the correct versions of AGP,
Kotlin and dependencies automatically.
If the app is obfuscated, we could also use [slackhq/keeper](https://github.com/slackhq/keeper)
to infer the obfuscation rules.

### Building The App Under Test (AUT)

Standalone test module:

```groovy
plugins {
    id "com.android.test"
}

android {
    namespace = "com.my.espresso.server"

    defaultConfig {
        testApplicationId = "io.appium.espressoserver.test"
        minSdk = 21
        targetSdk = 34
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation "androidx.test:runner:1.6.0"
    implementation "io.appium.espressoserver:library:<latest_driver_version>"
}
```

The test that should reside on the consumer side:

```kotlin
package com.my.espresso.server

import android.annotation.SuppressLint
import androidx.test.filters.LargeTest
import io.appium.espressoserver.lib.http.Server
import org.junit.Rule
import org.junit.Test

@LargeTest
class EspressoServerRunnerTest {
    @get:Rule
    val server = Server()

    @Test
    fun startEspressoServer() {
        server.run()
    }
}
```

Build both the app and Espresso server:

```bash
./gradlew :app:assembleDebug :espresso_server:assembleDebug
```

### Running Appium Tests

As soon as the application containing Espresso server is built it might be used
to run tests with appium-espresso-driver. The only requirements to run a test are
- The application build according to the above tutorial is already installed on the device
- The precompiled Espresso Server module version satisfies the driver version:
  - At least major versions should match
  - The driver version must not be older than the server version

Set the [skipServerInstallation](../README.md#driverserver) to
`true` in your test session capabilities to provide the Espresso driver
with the hint that Espresso server is already listening on the device under test.
