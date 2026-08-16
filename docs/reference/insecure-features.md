---
title: Insecure Features
---

Some [insecure driver features](https://appium.io/docs/en/latest/guides/security/) are disabled by
default. They can be enabled upon launching Appium as follows:
```
appium --allow-insecure espresso:<feature-name>
```
or
```
appium --relaxed-security
```

For other insecure feature names recognized by the Appium server, see
[their Appium docs reference page](https://appium.io/docs/en/latest/reference/cli/insecure-features/).

## `adb_listen_all_network`

Enables the underlying `adb` instance to listen on all network interfaces, not just `localhost`.
Equivalent to passing the `-a` flag to `adb`.

Available since driver version 6.2.0.

## `adb_screen_streaming`

Enables broadcasting the device screen over MJPEG using the `mobile: startScreenStreaming` execute
method.

## `adb_shell`

Enables shell-related functionality for the underlying `adb` instance. Can be used in execute
methods like `mobile: shell`.

## `chromedriver_autodownload`

Enables automatic download of a ChromeDriver binary compatible with the active webview. Only
relevant in a webview context.

## `emulator_console`

Enables execution of Android emulator telnet console interface commands using the
`mobile: execEmuConsoleCommand` execute method. Not relevant for real devices.

## `get_server_logs`

Enables retrieval of Appium server logs using [the getLogEvents endpoint](https://appium.io/docs/en/latest/reference/api/appium/#getlogevents)
and the `log.entryAdded` BiDi event.

## `set_stylus_handwriting`

Enables toggling the Android stylus handwriting feature using the `mobile: setStylusHandwriting`
execute method.

Available since driver version 7.1.0.
