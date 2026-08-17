---
title: Scripts
---

Appium drivers can include scripts for executing specific actions. The scripts included in the
Espresso driver can be run as follows:

```
appium driver run espresso <script-name>
```

For more information about the `appium driver run` command, refer to [the Appium docs](https://appium.io/docs/en/latest/reference/cli/extensions/#run).

!!! note

    Script arguments should be provided after an additional double dash (`--`), to ensure they are
    passed to the script itself, instead of the `appium driver run` command.

## `build-espresso`

Builds the Espresso server application. This can help to speed up session creation, as the driver
will be able to reuse the already-build server application.

Available since driver version 2.18.0.

### Usage

```
appium driver run espresso build-espresso
```

#### Optional Arguments

|<div style="width:10em">Argument</div>|Description|Type|Default|
|--|--|--|--|
|`--build-config`|Path to a JSON build configuration file. Must match the format of the [`appium:espressoBuildConfig`](./capabilities.md#espressobuildconfig) capability. Can alternatively be specified using the `ESPRESSO_BUILD_CONFIG` environment variable.|string||
|`--show-gradle-log`|Whether to show the output of the Gradle log. Can alternatively be specified using the `SHOW_GRADLE_LOG` environment variable.|boolean|`false`|
|`--test-app-package`|Name of the application package to build the server for (`applicationId`). Can alternatively be specified using the `TEST_APP_PACKAGE` environment variable.|string||

### Examples

- Build the Espresso server with default settings and no target application:

    ```
    appium driver run espresso build-espresso
    ```

- Build the server using custom settings, for the `com.mycompany.myapp` package:

    ```
    appium driver run espresso build-espresso -- --build-config=/path/to/config.json --test-app-package=com.mycompany.myapp
    ```

## `diagnose-app`

Analyses the provided application under test, identifies any potential automation-related issues,
and suggests fragments for the [`appium:espressoBuildConfig`](./capabilities.md#espressobuildconfig)
capability.

Available since driver version 8.6.0.

### Usage

```
appium driver run espresso diagnose-app -- --app=<app>
```

|Argument|Description|Type|
|--|--|--|
|`--app`|Path to either the Gradle project root of the app, or an already-built `.apk` file. If specifying the `.apk` path, it is recommended to use a debug APK.|string|

### Examples

- Diagnose the Gradle project at `/path/to/project`:

    ```
    appium driver run espresso diagnose-app -- --app=/path/to/project
    ```

- Diagnose the `.apk` file at `/path/to/app.apk`:

    ```
    appium driver run espresso diagnose-app -- --app=/path/to/app.apk
    ```

## `print-espresso-path`

Prints the path to the Espresso server directory.

Available since driver version 2.15.2.

### Usage

```
appium driver run espresso print-espresso-path
```
