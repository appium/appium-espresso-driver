---
title: Execute Methods
---

The Espresso driver provides various [custom execute methods](https://appium.io/docs/en/latest/guides/execute-methods/)
based on the standard Execute Script endpoint. Use the following examples in order to invoke them
from your client code:

=== "Java"

    ```java
    var result = driver.executeScript("mobile: <methodName>", Map.ofEntries(
        Map.entry("arg1", "value1"),
        Map.entry("arg2", "value2")
        // you may add more pairs if needed or skip providing the map completely
        // if all arguments are defined as optional
    ));
    ```

=== "JS (WebdriverIO)"

    ```js
    const result = await driver.executeScript('mobile: <methodName>', [{
        arg1: "value1",
        arg2: "value2",
    }]);
    ```

=== "Python"

    ```python
    result = driver.execute_script('mobile: <methodName>', {
        'arg1': 'value1',
        'arg2': 'value2',
    })
    ```

=== "Ruby"

    ```ruby
    result = @driver.execute_script 'mobile: <methodName>', {
        arg1: 'value1',
        arg2: 'value2',
    }
    ```

=== "C#"

    ```csharp
    object result = driver.ExecuteScript("mobile: <methodName>", new Dictionary<string, object>() {
        {"arg1", "value1"},
        {"arg2", "value2"}
    }));
    ```

### `mobile: shell`

Executes the specified `adb shell` command on the device under test. The [`adb_shell`](./insecure-features.md#adb_shell)
insecure feature must be enabled.

#### Parameters

|<div style="width:8em">Name</div>|<div style="width:8em">Type</div>|Description|
|---|---|---|
|`command`|`string`|Shell command name to execute|
|`args?`|`Array<string>`|Additional arguments to pass to the command. Keys and values should be provided as separate strings.|
|`timeout?`|`number`|Command timeout in milliseconds. An error is thrown if the command blocks for longer than this timeout. Set to `20000` by default|
|`includeStderr?`|`boolean`|Whether to include stderr stream into the returned result. Set to `false` by default|

#### Response

`string` or `Record<string, string>` - contents of the returned `stdout`, or if `includeStderr` is
true, an object with the `stdout` and `stderr` keys and their respective values.

An error is thrown if the command exits with a non-zero return code, with the error message
matching the command's `stderr`.

### `mobile: execEmuConsoleCommand`

Executes the specified command using the Android emulator telnet console interface. The
[`emulator_console`](./insecure-features.md#emulator_console) insecure feature must be enabled.

#### Parameters

|<div style="width:7em">Name</div>|Type|Description|
|---|---|---|
|`command`|`string` or `Array<string>`|Command name to execute. See [Android Emulator Console Guide](https://developer.android.com/studio/run/emulator-console) for more details on available commands|
|`execTimeout?`|`number`|Timeout in milliseconds to wait for the server to reply to the given command. Set to `60000` by default|
|`connTimeout?`|`number`|Console connection timeout in milliseconds. Set to `5000` by default|
|`initTimeout?`|`number`|Telnet console initialization timeout in milliseconds (the time between the connection being established and the command prompt becoming available). Set to `5000` ms by default|

#### Response

`string` - the command output. An error is thrown if command execution fails.

### `mobile: startLogsBroadcast`

Starts a websocket server for broadcasting Android logcat logs, using the host and port of the
Appium server. The resulting endpoint is `/ws/session/:sessionId:/appium/logcat`. Broadcasting can
be stopped using the [`mobile: stopLogsBroadcast`](#mobile-stoplogsbroadcast) execute method.

The method will return immediately if the web socket is already listening. Each listener will
receive logcat log lines as soon as they are visible to Appium.

Refer to [Using Mobile Execution Commands to Continuously Stream Device Logs with Appium](https://www.headspin.io/blog/using-mobile-execution-commands-to-continuously-stream-device-logs-with-appium)
for more details.

Available since driver version 2.37.0.

#### Response

`null`

### `mobile: stopLogsBroadcast`

Stops the logcat websocket server previously started using [`mobile: startLogsBroadcast`](#mobile-startlogsbroadcast).
The method will return immediately if no websocket server is running.

Refer to [Using Mobile Execution Commands to Continuously Stream Device Logs with Appium](https://www.headspin.io/blog/using-mobile-execution-commands-to-continuously-stream-device-logs-with-appium)
for more details.

Available since driver version 2.37.0.

#### Response

`null`

### `mobile: changePermissions`

Changes runtime permissions for a specified application package.

This function supports two modes, `pm` and `appops`, which can be distinguished using the `target`
parameter. Use of the `appops` mode requires the [`adb_shell`](./insecure-features.md#adb_shell)
insecure feature to be enabled.

#### Parameters

|<div style="width:7em">Name</div>|<div style="width:7em">Type</div>|Description|
|---|---|---|
|`permissions`|`string` or `Array<string>`|One or more permissions to be changed. For `pm` mode, supported values can be found in the [Android Manifest documentation](https://developer.android.com/reference/android/Manifest.permission), and the `all` magic string is additionally supported. For `appops` mode, supported values can be found in the [Android AppOpsManager documentation](https://developer.android.com/reference/android/app/AppOpsManager). Full constant values must be used for both modes.|
|`appPackage?`|`string`|Name of the application package to change. Set to the package of the app under test by default.|
|`action`|`string`|Permission action to apply. Supported values are either `grant` or `revoke` (in `pm` mode), or `allow`, `ignore`, `deny` and `default` (in `appops` mode).|
|`target?`|`string`|The mode to use. Supported values are `pm` and `appops`. Set to `pm` by default.|

#### Response

`null`

### `mobile: getPermissions`

Retrieves runtime permissions for a specified application package.

#### Parameters

|<div style="width:7em">Name</div>|Type|Description|
|---|---|---|
|`type?`|`string`|Type of permissions to retrieve. Supported values are `denied`, `granted` and `requested`. Set to `requested` by default.|
|`appPackage?`|`string`|Name of the application package to change. Set to the package of the app under test by default.|

#### Response

`Array<string>` - list of permission names. Could be empty

### `mobile: performEditorAction`

Performs IME action on the _currently focused_ editable element.

Very often Android developers use the [`onEditorAction`](https://developer.android.com/reference/android/widget/TextView.OnEditorActionListener.html#onEditorAction(android.widget.TextView,%20int,%20android.view.KeyEvent))
callback with the `actionId` argument to implement handling of actions, for example, when the
`Search` or `Done` button is pressed on the on-screen keyboard. This method aims to emulate the
invocation of such a callback on the focused element.

#### Parameters

|Name|Type|Description|
|---|---|---|
|`action`|`string` or `number`|Name or integer code of the editor action to be executed. Supported values are `unspecified`, `none`, `go`, `search`, `send`, `next`, `done`, and `previous`. Refer to the [Android EditorInfo](https://developer.android.com/reference/android/view/inputmethod/EditorInfo) documentation for more details.|

#### Response

`null`

### `mobile: getDeviceTime`

Retrieves the current system time on the device under test.

#### Parameters

|Name|Type|Description|
|---|---|---|
|`format?`|`string`|Format to return the timestamp in. Refer to the [`dayjs` documentation](https://day.js.org/docs/en/display/format) for the format syntax. Set to `YYYY-MM-DDTHH:mm:ssZ` by default, matching the ISO8601 format.|

#### Response

`string` - the device timestamp in the specified format

### `mobile: startScreenStreaming`

Starts an MJPEG server for broadcasting the screen of the device under test. The [`adb_screen_streaming`](./insecure-features.md#adb_screen_streaming)
insecure feature must be enabled, and the host machine must have [GStreamer](https://gstreamer.freedesktop.org/)
installed and available on `PATH`, along with the `gst-plugins-base`, `gst-plugins-good`,
`gst-plugins-bad` and `gst-libav` packages.

Broadcasting can be stopped using the [`mobile: stopScreenStreaming`](#mobile-stopscreenstreaming)
execute method. Repeated calls to this method have no effect unless the previous streaming session
is stopped.

#### Parameters

|<div style="width:11em">Name</div>|Type|Description|
|---|---|---|
|`width?`|`number`|Scaled width of the device screen. Set to the actual device screen width by default. |
|`height?`|`number`|Scaled height of the device screen. Set to the actual device screen height by default. |
|`bitRate?`|`number`|Bitrate of the video, in bits per second. Set to `4000000` (4 Mbps) by default.|
|`host?`|`string`|IP address/host name to start the MJPEG server on. Set to `127.0.0.1` by default. Can be set to `0.0.0.0` to broadcast on all available network interfaces.|
|`pathname?`|`string`|URL path on which the MJPEG server should be accessible. By default, all pathnames on the given `host`/`port` combination are accessible. Must begin with a forward slash (`/`).|
|`port?`|`number`|Port number to start the MJPEG server on. Set to `8093` by default.|
|`tcpPort?`|`number`|Port number to start the internal TCP MJPEG broadcast on. Always starts on the loopback interface (`127.0.0.1`). Set to `8094` by default.|
|`quality?`|`number`|Quality of the broadcasted images. Must be an integer in the range `1..100`, where `100` indicates the best quality. Set to `70` by default.|
|`considerRotation?`|`boolean`|Whether to increase the broadcast dimensions to fit both landscape and portrait orientations. Should be set to `true` if the device orientation will be changed during the broadcast. Set to `false` by default.|
|`logPipelineDetails?`|`boolean`|Whether to include GStreamer pipeline event logs into the standard log output. Can be useful for debugging purposes. Set to `false` by default.|

#### Response

`null`

### `mobile: stopScreenStreaming`

Stops the MJPEG screen broadcasting server previously started using [`mobile: startScreenStreaming`](#mobile-startscreenstreaming).
The method will return immediately if no streaming server is running.

#### Response

`null`

### `mobile: getNotifications`

Retrieves up to 100 most recent Android notifications, including dismissed ones. The Appium Settings
helper app must first be *manually* granted notification access.

#### Response

`Record<string, any>` - mapping of notification categories to arrays of notification objects. Newer
notifications are always added to the start of the array. For details on the notification object,
refer to the Android [StatusBarNotification](https://developer.android.com/reference/android/service/notification/StatusBarNotification)
and [Notification](https://developer.android.com/reference/android/app/Notification) documentation.
The `isRemoved` flag is set to `true` for dismissed notifications.

Example output:

```json
{
  "statusBarNotifications": [
    {
      "isGroup": false,
      "packageName": "io.appium.settings",
      "isClearable": false,
      "isOngoing": true,
      "id": 1,
      "tag": null,
      "notification": {
        "title": null,
        "bigTitle": "Appium Settings",
        "text": null,
        "bigText": "Keep this service running, so Appium for Android can properly interact with several system APIs",
        "tickerText": null,
        "subText": null,
        "infoText": null,
        "template": "android.app.Notification$BigTextStyle"
      },
      "userHandle": 0,
      "groupKey": "0|io.appium.settings|1|null|10133",
      "overrideGroupKey": null,
      "postTime": 1576853518850,
      "key": "0|io.appium.settings|1|null|10133",
      "isRemoved": false
    }
  ]
}
```

### `mobile: listSms`

Retrieves the most recent SMS messages.

#### Parameters

|Name|Type|Description|
|---|---|---|
|`max?`|`number`|Maximum number of messages to retrieve. Set to `100` by default.|

#### Response

`Record<string, any>` - map containing an array of notification objects, and their count. Newer
messages are always added to the start of the array.

Example output:

```json
 {
  "items": [
    {
      "id": "2",
      "address": "+123456789",
      "person": null,
      "date": "1581936422203",
      "read": "0",
      "status": "-1",
      "type": "1",
      "subject": null,
      "body": "\"text message2\"",
      "serviceCenter": null
    },
    {
      "id": "1",
      "address": "+123456789",
      "person": null,
      "date": "1581936382740",
      "read": "0",
      "status": "-1",
      "type": "1",
      "subject": null,
      "body": "\"text message\"",
      "serviceCenter": null
    }
  ],
  "total": 2
 }
```

### `mobile: pushFile`

Pushes data to a file on the device under test. If the target file already exists, its contents
will be overwritten.

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|---|---|---|
|`remotePath`|`string`|Full path to the file where the data should be written to, or a path inside an app bundle (e.g. `@my.app.id/my/path`). The latter format requires the target app bundle to have debugging enabled. An error is thrown if the path resolves to a directory.|
|`payload`|`string`|Base64-encoded data to be included in the file|

#### Response

`null`

### `mobile: pullFile`

Pulls the contents of a file from the device under test.

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|---|---|---|
|`remotePath`|`string`|Full path to the file where the data should be retrieved from, or a path inside an app bundle (e.g. `@my.app.id/my/path`). The latter format requires the target app bundle to have debugging enabled. An error is thrown if the path resolves to a directory.|

#### Response

`string` - Base64-encoded contents of the specified file

### `mobile: pullFolder`

Pulls the contents of a directory from the device under test.

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|---|---|---|
|`remotePath`|`string`|Full path to a directory on the device under test|

#### Response

`string` - Base64-encoded zipped contents of the specified directory

### `mobile: deleteFile`

Deletes a file from the device under test.

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|---|---|---|
|`remotePath`|`string`|Full path to the file to be deleted, or a path inside an app bundle (e.g. `@my.app.id/my/path`). The latter format requires the target app bundle to have debugging enabled. An error is thrown if the path resolves to a directory.|

#### Response

`boolean` - `true` if the file was successfully deleted, `false` if it does not exist

### `mobile: isAppInstalled`

Determines whether the application with the specified package identifier is installed on the device
under test.

#### Parameters

|Name|Type|Description|
|---|---|---|
|`appId`|`string`|Package identifier of the application|
|`user?`|`number` or `string`|ID of the user for which the app is installed. The `current` user is used by default|

#### Response

`boolean` - `true` if the app is installed, otherwise `false`

### `mobile: listApps`

Retrieves information about installed applications on the device under test. Only supported since
Android 8 (Oreo / API level 26).

#### Parameters

|Name|Type|Description|
|---|---|---|
|`user?`|`number` or `string`|ID of the user to filter the installed packages for|

#### Response

`Record<string, Record<string, any>>` - mapping of package names to their details. The
`versionCode` property is only populated for devices running Android 9 (Pie / API level 28) or
later.

In Espresso driver versions before 7.0.0, the response was `Array<string>` - a list of package
names.

### `mobile: queryAppState`

Retrieves the state of the application with the specified package identifier on the device under
test.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`appId`|`string`|Package identifier of the app to query|

#### Response

`number` - an integer indicating the app state:

|Number|Description|
|--|--|
|`0`|Not installed|
|`1`|Not running|
|`3`|Running in background|
|`4`|Running in foreground|

### `mobile: activateApp`

Activates the application with the specified package identifier or launches it if necessary, by
simulating a tap on the app icon on the Android UI.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`appId`|`string`|Package identifier of the app to activate|

#### Response

`null`

### `mobile: removeApp`

Uninstalls the application with the specified package identifier from the device under test.

#### Parameters

|<div style="width:10em">Name</div>|Type|Description|
|--|--|--|
|`appId`|`string`|Package identifier of the app to uninstall|
|`timeout?`|`number`|Number of milliseconds to wait until the app is terminated before uninstallation. Set to `20000` by default.|
|`keepData?`|`boolean`|Whether to retain application data and cache after uninstall. Unset by default.|
|`skipInstallCheck?`|`boolean`|Whether to check if the app is installed before uninstalling it. Set to `true` by default.|

#### Response

`boolean` - `true` if the app was removed, otherwise `false`

### `mobile: terminateApp`

Terminates the application with the specified package identifier and waits until its app process
has stopped.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`appId`|`string`|Package identifier of the app to terminate|
|`timeout?`|`number`|Number of milliseconds to wait until the app is terminated. Set to `500` by default. Since driver version 2.13.0, setting this to `0` or a negative value skips the app state check.|

#### Response

`boolean` - `true` if the app was terminated, otherwise `false`

### `mobile: installApp`

Installs the specified application on the device under test. 

If a newer version of the application was already installed, the `INSTALL_FAILED_VERSION_DOWNGRADE`
error may be raised.

#### Parameters

|<div style="width:10em">Name</div>|Type|Description|
|--|--|--|
|`appPath`|`string`|Full path to a file on the host machine, or URL to a remote location. The app must have the `.apk` or `.apks` extension.|
|`checkVersion?`|`boolean`|Whether to skip installation if an identical or newer app version is already installed. Unset by default. Applied before `replace`.|
|`timeout?`|`number`|Number of milliseconds to wait until the app is installed. Set to `60000` by default, unless overridden using the [`appium:adbExecTimeout`](./capabilities.md#adbexectimeout) capability|
|`allowTestPackages?`|`boolean`|Whether to allow installation of test packages. Set to `false` by default|
|`useSdcard?`|`boolean`|Whether to install the app on the SD card instead of built-in storage. Set to `false` by default|
|`grantPermissions?`|`boolean`|Whether to automatically grant all permissions defined in the application manifest after installation. Set to `false` by default. Only supported on Android 6 (Marshmallow / API level 23) or later.|
|`replace?`|`boolean`|Whether to replace any already existing app installation. Set to `true` by default. If set to `false` and the app is already installed, an error is thrown. Applied after `checkVersion`.|
|`noIncremental?`|`boolean`|Whether to disable incremental app installation. Set to `false` by default. Refer to [How ADB incremental-install works](https://android.googlesource.com/platform/packages/modules/adb/+/HEAD/docs/dev/incremental-install.md) for more details.|

#### Response

`null`

### `mobile: clearApp`

Clears all data associated with the application with the specified package identifier: user data,
cache, and settings. Calls `adb shell pm clear <appId>` under the hood.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`appId`|`string`|Package identifier of the app to clear|

#### Response

`null`

### `mobile: backgroundApp`

Moves the active app to the background and optionally restores it into the foreground after a
specified duration. The call is blocking.

Available since driver version 2.23.0.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`seconds`|`number`|Number of seconds after which to restore the app to foreground. If set to `0` or a negative value, automatic restoration is skipped.|

#### Response

`null`

### `mobile: broadcast`

Sends a broadcast Intent to the Android system. Invokes `adb shell am broadcast` under the hood.

#### Parameters

|<div style="width:16em">Name</div>|<div style="width:11em">Type</div>|Description|
|--|--|--|
|`intent?`|`string`|Full name of the intent to broadcast|
|`user?`|`number` or `string`|ID of the user to send the broadcast to|
|`action?`|`string`|Name of the intent action|
|`uri?`|`string`|Intent URI|
|`mimeType?`|`string`|Intent MIME type|
|`identifier?`|`string`|Intent identifier|
|`categories?`|`string` or `Array<string>`|One or more intent categories|
|`component?`|`string`|Intent component|
|`package?`|`string`|Intent package name|
|`extras?`|`Array<Array<string>>`|Extra intent arguments. See below for expected structure|
|`flags?`|`string`|Flags in hexadecimal format to apply on intent start-up. Refer to the [Android Intent documentation](https://developer.android.com/reference/android/content/Intent) for supported values. Multiple flags should be merged into one value.|
|`receiverPermission?`|`string`|Permission that the receiver must hold|
|`allowBackgroundActivityStarts?`|`boolean`|Whether the receiver may start activities even if in the background|

The `flags` parameter is an array of arrays, where each subarray contains 3 items: value category,
key, and the value itself. Supported value categories and their value types are as follows:

|Value Category|Value Type|
|--|--|
|`s`|String|
|`sn`|Null (only the key is required; the value should be omitted)|
|`z`|Boolean|
|`i`|Integer|
|`l`|Long integer|
|`f`|Float|
|`u`|URI|
|`cn`|Component name (string)|
|`ia`|String of comma-separated integers|
|`ial`|String of comma-separated integers|
|`la`|String of comma-separated long integers|
|`lal`|String of comma-separated long integers|
|`fa`|String of comma-separated floats|
|`fal`|String of comma-separated floats|
|`sa`|Comma-separated strings (commas that are part of the strings themselves should be escaped)|
|`sal`|Comma-separated strings (commas that are part of the strings themselves should be escaped)|

#### Response

`string` - output of the `adb shell am broadcast` command.

### `mobile: getContexts`

Retrieves a detailed list of available webview contexts with their mapping information. Does not
include non-webview (e.g. native) contexts.

#### Parameters

|<div style="width:10em">Name</div>|Type|Description|
|--|--|--|
|`waitForWebviewMs?`|`number`|Number of milliseconds for how long to retry retrieval of webview data. Set to `0` by default. Higher values can help prevent ChromeDriver errors such as `failed to connect to socket 'localabstract:chrome_devtools_remote'`. Refer to [this issue](https://github.com/appium/appium/issues/19251) for more details. Available since driver version 2.30.0.|

#### Response

`Array<Record<string, any>>` - list of webview objects. Example output:

```json
[
  {
    "proc": "@webview_devtools_remote_22138",
    "webview": "WEBVIEW_22138",
    "info": {
      "Android-Package": "io.appium.settings",
      "Browser": "Chrome/74.0.3729.185",
      "Protocol-Version": "1.3",
      "User-Agent": "Mozilla/5.0 (Linux; Android 10; Android SDK built for x86 Build/QSR1.190920.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.185 Mobile Safari/537.36",
      "V8-Version": "7.4.288.28",
      "WebKit-Version": "537.36 (@22955682f94ce09336197bfb8dffea991fa32f0d)",
      "webSocketDebuggerUrl": "ws://127.0.0.1:10900/devtools/browser"
    },
    "pages": [
      {
        "description": "{\"attached\":true,\"empty\":false,\"height\":1458,\"screenX\":0,\"screenY\":336,\"visible\":true,\"width\":1080}",
        "devtoolsFrontendUrl": "http://chrome-devtools-frontend.appspot.com/serve_rev/@22955682f94ce09336197bfb8dffea991fa32f0d/inspector.html?ws=127.0.0.1:10900/devtools/page/27325CC50B600D31B233F45E09487B1F",
        "id": "27325CC50B600D31B233F45E09487B1F",
        "title": "Releases · appium/appium · GitHub",
        "type": "page",
        "url": "https://github.com/appium/appium/releases",
        "webSocketDebuggerUrl": "ws://127.0.0.1:10900/devtools/page/27325CC50B600D31B233F45E09487B1F"
      }
    ],
    "webviewName": "WEBVIEW_com.io.appium.setting"
  }
]
```

### `mobile: getChromeCapabilities`

Retrieves the current ChromeDriver session capabilities. Only supported in a webview context. Can
be useful for debugging Chrome/webview automation issues and understanding what capabilities are
being applied to the Chromedriver instance.

Available since driver version 6.0.7.

#### Response

`Record<string, any>` - map of current ChromeDriver capabilities, typically consisting of standard
W3C WebDriver capabilities. The exact structure may depend on the Chrome/ChromeDriver version and
the initial session capabilities.

### `mobile: lock`

Locks the device and optionally unlocks it after a specified duration. Only simple (e.g. without a
password) locks are supported.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`seconds?`|`number`|Number of seconds after which to unlock the device. If omitted or set to `0`, automatic unlock is skipped.|

#### Response

`null`

### `mobile: unlock`

Unlocks the device if it is locked. Refer to [the Unlock guide](../guides/unlock.md) for more
details.

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|--|--|--|
|`key?`|`number`|The unlock key. By default, set to the value of the [`appium:unlockKey`](./capabilities.md#unlockkey) capability. Must be provided together with `type`.|
|`type?`|`number`|The unlock type. By default, set to the value of the [`appium:unlockType`](./capabilities.md#unlocktype) capability. Supported values are `pin`, `pinWithKeyEvent`, `password`, `pattern` and `fingerprint`. Must be provided together with `key`.|
|`strategy?`|`number`|Approach to use for unlocking. Unset by default. If unset or set to `locksettings`, uses an `adb`-based fast unlock approach, otherwise uses `type`-specific approaches.|
|`timeoutMs?`|`number`|The unlock timeout. By default, set to the value of the [`appium:unlockSuccessTimeout`](./capabilities.md#unlocksuccesstimeout) capability.|

#### Response

`null`

### `mobile: isLocked`

Determines whether the device is locked.

#### Response

`boolean` - `true` if the device is locked, otherwise `false`

### `mobile: refreshGpsCache`

Sends a request to refresh the GPS cache on the device under test. By default, location tracking is
configured for [low battery consumption](https://github.com/appium/io.appium.settings/blob/master/app/src/main/java/io/appium/settings/LocationTracker.java),
so this method can be useful if the device location frequently changes.

The device under test must either have Google Play Services installed, or be running Android 11
(R / API level 30) or later (which relies on [LocationManager](https://developer.android.com/reference/android/location/LocationManager)).

#### Parameters

|<div style="width:6em">Name</div>|Type|Description|
|--|--|--|
|`timeoutMs?`|`number`|Maximum number of milliseconds to block until the GPS cache is confirmed to have been refreshed. Set to `20000` by default. An error is thrown if the device does not return a successful cache refresh response within this timeout. If set to `0` or a negative value, waiting is skipped.|

#### Response

`null`

### `mobile: startMediaProjectionRecording`

Starts recording the device screen and audio using Android's [Media Projection](https://developer.android.com/reference/android/media/projection/MediaProjection)
API. The device under test must be running Android 10 (Q / API level 29) or later. Recording can be
stopped using the [`mobile: stopMediaProjectionRecording`](#mobile-stopmediaprojectionrecording)
execute method.

#### Parameters

|<div style="width:9em">Name</div>|Type|Description|
|--|--|--|
|`resolution?`|`string`|Resolution of the resulting video, formatted as `<width>x<height>`. Supported values are `1920x1080`, `1280x720`, `720x480`, `320x240` and `176x144`. Set to the greatest supported device resolution by default (usually `1920x1080`).|
|`priority?`|`string`|Priority of the recorder process, which could be adjusted in case of performance drops. Supported values are `high`, `normal` and `low`. Set to `high` by default.|
|`maxDurationSec?`|`number`|Maximum recording time in seconds. Set to `900` (15 minutes) by default. |
|`filename?`|`string`|Name of the resulting video file. The `.mp4` extension is added automatically if absent. Set to the current timestamp by default.|

#### Response

`boolean` - `true` if a new recording has successfully started, otherwise `false`

### `mobile: isMediaProjectionRecordingRunning`

Determines whether a Media Projection-based recording is currently active. The device under test
must be running Android 10 (Q / API level 29) or later.

#### Response

`boolean` - `true` if recording is active, otherwise `false`

### `mobile: stopMediaProjectionRecording`

Stops the active screen recording process started by [`mobile: startMediaProjectionRecording`](#mobile-startmediaprojectionrecording),
either returning its payload or uploading it to a remote location. The device under test must be
running Android 10 (Q / API level 29) or later.

If the recording process is not running, but another recording has previously finished, its data is
used instead. If no previous recording was found, an error is thrown.

#### Parameters

|<div style="width:8em">Name</div>|<div style="width:8em">Type</div>|Description|
|--|--|--|
|`remotePath?`|`string`|Path to a remote location where the resulting video file should be uploaded. Supported path protocols are HTTP(S) and FTP (deprecated). An exception is thrown if the file is too big to fit in the process memory.|
|`user?`|`string`|Username used for authentication to `remotePath`|
|`pass?`|`string`|Password used for authentication to `remotePath`|
|`method?`|`string`|Name of the HTTP(S) multipart upload method. Set to `PUT` by default.|
|`headers?`|`Record<string, any>`|Additional headers to use for the HTTP(S) multipart upload|
|`fileFieldName?`|`string`|Name of the form field for storing the file content blob for HTTP(S) uploads. Set to `file` by default.|
|`formFields?`|`Record<string, any>` or `Array<[string, any]>`|Additional form fields to use for the HTTP(S) multipart upload|

#### Response

`string` - the Base64-encoded string of the screen recording, or an empty string if `remotePath` is
set

### `mobile: getConnectivity`

Retrieves the state of one or more connectivity-related system services.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`services?`|`string` or `Array<string>`|One or more service names to check. Supported values are `wifi`, `data`, and `airplaneMode`. By default, the states for all services are returned.|

#### Response

`Record<string, boolean>` - mapping of service names to whether they are enabled. If the `services` parameter was set, only the specified services are returned.

### `mobile: setConnectivity`

Sets the state of one or more connectivity-related system services. On real devices, switching WiFi
only works reliably since Android 11 (R / API level 30).

!!! warning

    Using this method may result in Android terminating/disconnecting the Espresso server app on
    the device under test, causing a session disconnect. The only way to restore the session
    afterwards would be to quit it, restore device connectivity, then reconnect to it with the
    `noReset` capability set to `true`.

#### Parameters

At least one of the below parameters must be provided. If any parameter is not specified, the state
of its service remains unchanged.

|Name|Type|Description|
|--|--|--|
|`wifi?`|`boolean`|Whether WiFi should be enabled or disabled|
|`data?`|`boolean`|Whether mobile data should be enabled or disabled|
|`airplaneMode?`|`boolean`|Whether Airplane Mode should be enabled or disabled|

#### Response

`null`

### `mobile: hideKeyboard`

Hides the on-screen keyboard. An error is thrown if the keyboard cannot be hidden.

#### Response

`boolean` - `true` if the keyboard was successfully hidden, otherwise `false`

### `mobile: isKeyboardShown`

Determines whether the on-screen keyboard is shown.

#### Response

`boolean` - `true` if the keyboard is shown, otherwise `false`

### `mobile: deviceidle`

Adds or removes one or more applications from the Android system whitelist for apps that should not
be forced into a limited mode after a period of inactivity (in other words, apps that should not be
automatically put to sleep). Calls `adb shell dumpsys deviceidle` under the hood. Only supported
since Android 6 (Marshmallow / API level 23).

Refer to the [Diving Into Android 'M' Doze](https://www.protechtraining.com/blog/post/diving-into-android-m-doze-875)
guide for more details.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`action`|`string`|Action to apply for the whitelist. Supported values are `whitelistAdd` and `whitelistRemove`.|
|`packages`|`string` or `Array<string>`|One or more package identifiers to add or remove from the whitelist|

#### Response

`null`

### `mobile: bluetooth`

Performs the specified action on the Android system Bluetooth adapter. An error is thrown if the
device under test does not have a Bluetooth adapter.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`action`|`string`|Bluetooth action to apply. Supported values are `enable`, `disable`, and `unpairAll`.|

#### Response

`null`

### `mobile: nfc`

Performs the specified action on the Android system NFC adapter. An error is thrown if the device
under test does not have a NFC adapter.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`action`|`string`|NFC action to apply. Supported values are `enable` and `disable`.|

#### Response

`null`

### `mobile: setUiMode`

Sets the device appearance mode. Only supported since Android 10 (Q / API level 29). Calls
`adb shell cmd uimode` under the hood.

Available since driver version 2.29.0.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`mode`|`string`|Appearance mode to set. Supported values are `night` and `car`.|
|`value`|`string`|Value of the specified `mode`. Supported values are either `yes`, `no`, `auto`, `custom_schedule`, and `custom_bedtime` (for `night` mode), or `yes` and `no` (for `car` mode).|

#### Response

`null`

### `mobile: getUiMode`

Retrieves the value for the specified device appearance mode. Only supported since Android 10 (Q /
API level 29). Calls `adb shell cmd uimode` under the hood.

Available since driver version 2.29.0.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`mode`|`string`|Appearance mode to check. Supported values are `night` and `car`.|

#### Response

`string` - value of the specified appearance mode
