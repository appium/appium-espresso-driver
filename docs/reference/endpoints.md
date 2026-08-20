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

|Name|Description|Type|
|--|--|--|
|`engine`|Name of the IME engine to activate|`string`|

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

|Name|Description|Type|
|--|--|--|
|`value`|Keys to be sent|`string`|

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

|Name|Description|Type|
|--|--|--|
|`altitude`|Altitude of the device location|number|
|`latitude`|Latitude of the device location|number|
|`longitude`|Longitude of the device location|number|

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

|Name|Description|Type|
|--|--|--|
|`location`|New device latitude, longitude and altitude|[`Location`](#response_10)|

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

|<div style="width:6em">Name</div>|Description|<div style="width:18em">Type</div>|
|--|--|--|
|`parameters`|Object containing the `type` key, whose value is the desired network state|`{"type": `[`NetworkConnectionState`](#response_12)`}`|

#### Response

[`NetworkConnectionState`](#response_12) - the new network state

## Appium Protocol

### getClipboard

```
POST /session/:sessionId/appium/device/get_clipboard
```

Retrieves the content of the primary clipboard on the device under test.

!!! warning "Deprecated"

    Please use the [`mobile: getClipboard`](./execute-methods.md#mobile-getclipboard) execute
    method instead

#### Parameters

|<div style="width:7em">Name</div>|Description|Type|
|--|--|--|
|`contentType?`|The type to retrieve the content as. The only supported and default value is `plaintext`.|`string`|

#### Response

`string` - the clipboard content as a Base64 string. An empty string is returned if the clipboard
contains no data.
