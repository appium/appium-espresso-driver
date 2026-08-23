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

#### Response

`null`

### `mobile: stopLogsBroadcast`

Stops the logcat websocket server previously started using [`mobile: startLogsBroadcast`](#mobile-startlogsbroadcast).
The method will return immediately if no websocket server is running.

Refer to [Using Mobile Execution Commands to Continuously Stream Device Logs with Appium](https://www.headspin.io/blog/using-mobile-execution-commands-to-continuously-stream-device-logs-with-appium)
for more details.

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
