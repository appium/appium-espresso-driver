---
title: Espresso Server as a Library
---

Since driver version 3.0.0, it is possible to consume the Espresso server application as a library,
apply dependency constraints to align dependency versions, and embed it either as an `androidTest`
component of the app, or as a standalone test module under the same Gradle project.

The library only has to expose a single method for starting a server and a `TestRule` for Compose
support. This way, the Espresso server APK could be automatically built with the correct versions
of AGP, Kotlin and dependencies. If the app is obfuscated, it is also possible to use
[`slackhq/keeper`](https://github.com/slackhq/keeper) to infer the obfuscation rules.

## Building the App Under Test

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

## Running Appium Tests

As soon as the application containing the Espresso server is built, it can be used to run tests
with this driver. The only requirements to run a test are:

- The application build is installed on the device, following the above tutorial
- The precompiled Espresso Server module version satisfies the driver version:
    - At least major versions should match
    - The driver version must not be older than the server version

Make sure to set the [`appium:skipServerInstallation`](../reference/capabilities.md#skipserverinstallation)
capability to `true` in order to let the driver know that the Espresso server is already listening
on the device under test.
