---
title: Environment Variables
---

The driver recognizes several environment variables, which can be set when launching the Appium
server.

For other environment variables recognized by the Appium server, see
[their Appium docs reference page](https://appium.io/docs/en/latest/reference/cli/env-vars/).

## `APPIUM_PREFER_SYSTEM_UNZIP`

Can be set to either `0` or `false` to use the built-in Node.js unzipper over the native system
unzip utility. Mostly used for debugging purposes or troubleshooting, as the system unzip utility
is more performant in comparison to the built-in one.

Available since driver version 2.1.2.

## `ESPRESSO_JAVA_DEBUG`

Can be set to `true` to enable debug logging output from the Espresso server.
