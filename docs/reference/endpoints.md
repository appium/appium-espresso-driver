---
title: Endpoints
---

The Espresso driver comes with a set of many available endpoints, which are primarily inherited from
the Appium base driver, and can be found in [their Appium docs reference pages](https://appium.io/docs/en/latest/reference/api/).
Refer to the documentation of your Appium client for how to call specific endpoints.

The driver also defines several additional endpoints listed below. Please note that most of the
driver-specific functionality is available using [Execute Methods](./execute-methods.md) instead.

## JSON Wire Protocol

### availableIMEEngines

```
GET /session/:sessionId/ime/available_engines
```

Retrieves all IME (input method editor) engines available on the device under test.

#### Response

`string[]` - a list of available IME engines

### getActiveIMEEngine

```
GET /session/:sessionId/ime/active_engine
```

Retrieves the name of the active IME engine.

#### Response

`string` - the name of the active IME engine

### isIMEActivated

```
GET /session/:sessionId/ime/activated
```

Determines if IME input is available and active.

#### Response

`boolean` - `true` if IME is active, otherwise `false`

### deactivateIMEEngine

```
POST /session/:sessionId/ime/deactivate
```

Deactivates the currently active IME engine.

#### Response

`null`

### activateIMEEngine

```
POST /session/:sessionId/ime/activate
```

Activates an IME engine.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`engine`|`string`|Name of the IME engine to activate|

#### Response

`null`

### getWindowSize

```
GET /session/:sessionId/window/:windowhandle/size
```

Retrieves the size of the current window. The `:windowhandle` property is ignored, as the driver
always uses the currently active window.

!!! warning "Deprecated"

    Please use the [getWindowRect](https://appium.io/docs/en/latest/reference/api/webdriver/#getwindowrect) endpoint instead

#### Response

`Record<string, number>` - object containing the `width` and `height` properties of the current
window

### keys

```
POST /session/:sessionId/keys
```

Sends a sequence of key strokes to the active element.

!!! warning "Deprecated"

    Please use the `keyUp` and `keyDown` W3C Actions instead

#### Parameters

|Name|Type|Description|
|--|--|--|
|`value`|`string`|Keys to be sent|

#### Response

`null`

### getLocation

```
GET /session/:sessionId/element/:elementId/location
```

Returns the element's location on the page.

!!! warning "Deprecated"

    Please use the [getElementRect](https://appium.io/docs/en/latest/reference/api/webdriver/#getelementrect) endpoint instead

#### Response

`Record<string, number>` - object containing the `x` and `y` values of the element's top-left
coordinates

### getLocationInView

```
GET /session/:sessionId/element/:elementId/location_in_view
```

Returns the element's location on the page screen once it has been scrolled into view.

!!! warning "Deprecated"

    Please use the [getElementRect](https://appium.io/docs/en/latest/reference/api/webdriver/#getelementrect) endpoint instead

#### Response

`Record<string, number>` - object containing the `x` and `y` values of the element's top-left
coordinates

### getSize

```
GET /session/:sessionId/element/:elementId/size
```

Returns the element's size in pixels.

!!! warning "Deprecated"

    Please use the [getElementRect](https://appium.io/docs/en/latest/reference/api/webdriver/#getelementrect) endpoint instead

#### Response

`Record<string, number>` - object containing the `width` and `height` properties of the element

### getGeoLocation

```
GET /session/:sessionId/location
```

> JSONWP documentation: [/session/:sessionId/location](https://www.selenium.dev/documentation/legacy/json_wire_protocol/#sessionsessionidlocation)

Retrieves the current location of the device under test.

!!! warning "Deprecated"

    Please use the [`mobile: getGeolocation`](./execute-methods.md#mobile-getgeolocation) execute
    method instead

#### Response

`Location` - an object with the following properties:

|Name|Type|Description|
|--|--|--|
|`altitude`|`number`|Altitude of the device location|
|`latitude`|`number`|Latitude of the device location|
|`longitude`|`number`|Longitude of the device location|

### setGeoLocation

```
POST /session/:sessionId/location
```

> JSONWP documentation: [/session/:sessionId/location](https://www.selenium.dev/documentation/legacy/json_wire_protocol/#sessionsessionidlocation)

Sets the current location of the device under test.

!!! warning "Deprecated"

    Please use the [`mobile: setGeolocation`](./execute-methods.md#mobile-setgeolocation) execute
    method instead

#### Parameters

|Name|Type|Description|
|--|--|--|
|`location`|[`Location`](#response_10)|New device latitude, longitude and altitude|

#### Response

`null`

## Mobile JSON Wire Protocol

### getNetworkConnection

```
GET /session/:sessionId/network_connection
```

> MJSONWP documentation: [Device Modes](https://github.com/SeleniumHQ/mobile-spec/blob/master/spec-draft.md#device-modes)

Retrieves the current state of network types (data, Wi-Fi, airplane mode).

!!! warning "Deprecated"

    Please use the [`mobile: getConnectivity`](./execute-methods.md#mobile-getconnectivity) execute
    method instead

#### Response

`NetworkConnectionState` - a number indicating the current network state:

|Value|Data|Wi-Fi|Airplane Mode|
|--|--|--|--|
|`0`|OFF|OFF|OFF|
|`1`|OFF|OFF|ON|
|`2`|OFF|ON|OFF|
|`4`|ON|OFF|OFF|
|`6`|ON|ON|OFF|

### setNetworkConnection

```
POST /session/:sessionId/network_connection
```

> MJSONWP documentation: [Device Modes](https://github.com/SeleniumHQ/mobile-spec/blob/master/spec-draft.md#device-modes)

Sets the state of network types (data, Wi-Fi, airplane mode).

!!! warning "Deprecated"

    Please use the [`mobile: setConnectivity`](./execute-methods.md#mobile-setconnectivity) execute
    method instead

#### Parameters

|<div style="width:6em">Name</div>|<div style="width:18em">Type</div>|Description|
|--|--|--|
|`parameters`|`{"type": `[`NetworkConnectionState`](#response_12)`}`|Object containing the `type` key, whose value is the desired network state|

#### Response

[`NetworkConnectionState`](#response_12) - the new network state

## Appium Protocol

### startRecordingScreen

```
POST /session/:sessionId/appium/start_recording_screen
```

Starts recording the device screen using Android's `screenrecord` tool. On emulators this
functionality is only supported starting from Android 9 (Pie / API level 28). The recording can be
stopped either using the [`stopRecordingScreen`](#stoprecordingscreen) endpoint, or by stopping the
session itself.

#### Parameters

|Name|Type|Description|
|--|--|--|
|`options?`|`Record<string, any>`|Options for starting the screen recording|

The following keys are supported:

|<div style="width:8em">Name</div>|Type|Description|
|--|--|--|
|`videoSize?`|`string`|Dimensions of the resulting video, formatted as `<width>x<height>`. By default, the device's native display resolution is used, or `1280x720` if the native resolution is unsupported. For best results, use a size supported by your device's AVC encoder.|
|`bugReport?`|`boolean`|Whether to add a video overlay with debugging information, such as a timestamp. Only supported since Android 9 (Pie / API level 28).|
|`timeLimit?`|`number` or `string`|Maximum recording time in seconds. Set to `180` (3 minutes) by default. The maximum supported value is `1800` seconds (30 minutes). A single recording chunk can be at most `180` seconds (3 minutes) long, so if a greater value is specified, the driver will attempt to use multiple chunks and combine them using `ffmpeg`. In such cases, if `ffmpeg` is not available on `PATH`, only the most recent chunk will be retained.|
|`bitRate?`|`number` or `string`|Bitrate of the video, in bits per second. Set to `20000000` (20Mbps) by default.|
|`forceRestart?`|`boolean`|Whether to skip returning the results of any currently running screenrecording process, and start a new one right away|

If `forceRestart` is `false` or unset (the default value), all the keys supported by
`stopRecordingScreen` can also be used, in order to handle the upload of the result from the
currently running screenrecord process.

#### Response

`string` - the Base64-encoded string of a previous screen recording, if one existed and
`forceRestart` and `remotePath` were not set, otherwise an empty string

### stopRecordingScreen

```
POST /session/:sessionId/appium/stop_recording_screen
```

Stops the active screen recording process started by [`startRecordingScreen`](#startrecordingscreen),
either returning its payload or uploading it to a remote location. On emulators this functionality
is only supported starting from Android 9 (Pie / API level 28).

#### Parameters

|Name|Type|Description|
|--|--|--|
|`options?`|`Record<string, any>`|Options for stopping the screen recording|

The following keys are supported:

|<div style="width:8em">Name</div>|<div style="width:8em">Type</div>|Description|
|--|--|--|
|`remotePath?`|`string`|Path to a remote location where the resulting video file should be uploaded. Supported path protocols are HTTP(S) and FTP (deprecated). An exception is thrown if the file is too big to fit in the process memory.|
|`user?`|`string`|Username used for authentication to `remotePath`|
|`pass?`|`string`|Password used for authentication to `remotePath`|
|`method?`|`string`|Name of the HTTP(S) multipart upload method. Set to `POST` by default.|
|`headers?`|`Record<string, any>`|Additional headers to use for the HTTP(S) multipart upload|
|`fileFieldName?`|`string`|Name of the form field for storing the file content blob for HTTP(S) uploads. Set to `file` by default.|
|`formFields?`|`Record<string, any>` or `Array<[string, any]>`|Additional form fields to use for the HTTP(S) multipart upload|

#### Response

`string` - the Base64-encoded string of the screen recording, or an empty string if `remotePath` is
set or no active screen recording process is found

### getClipboard

```
POST /session/:sessionId/appium/device/get_clipboard
```

Retrieves the content of the primary clipboard on the device under test.

!!! warning "Deprecated"

    Please use the [`mobile: getClipboard`](./execute-methods.md#mobile-getclipboard) execute
    method instead

#### Parameters

|<div style="width:7em">Name</div>|Type|Description|
|--|--|--|
|`contentType?`|`string`|The type to retrieve the content as. The only supported and default value is `plaintext`.|

#### Response

`string` - the clipboard content as a Base64 string. An empty string is returned if the clipboard
contains no data.

### lock

```
POST /session/:sessionId/appium/device/lock
```

Locks the device (and optionally unlock it after a certain amount of time). Only simple (e.g.
without a password) locks are supported.

!!! warning "Deprecated"

    Please use the [`mobile: lock`](./execute-methods.md#mobile-lock) execute method instead

#### Parameters

|Name|Type|Description|
|--|--|--|
|`seconds?`|`number`|Number of seconds after which to unlock the device. If omitted or set to `0`, automatic unlock is skipped.|

#### Response

`null`

### unlock

```
POST /session/:sessionId/appium/device/unlock
```

Unlocks the device if it is locked. Only simple (e.g. without a password) locks are supported.

!!! warning "Deprecated"

    Please use the [`mobile: unlock`](./execute-methods.md#mobile-unlock) execute method instead

#### Response

`null`

### isLocked

```
POST /session/:sessionId/appium/device/is_locked
```

Determines whether the device is locked.

!!! warning "Deprecated"

    Please use the [`mobile: isLocked`](./execute-methods.md#mobile-islocked) execute method instead

#### Response

`boolean` - `true` if the device is locked, otherwise `false`
