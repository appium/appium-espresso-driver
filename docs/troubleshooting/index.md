---
title: Troubleshooting
---

As the Espresso driver is a grey-box driver with tighter coupling to the app under test (AUT), it
is fairly common to run into one or more issues. Usually, problems manifest immediately on session
startup, and may have many different causes.

It is generally recommended to use the [`diagnose-app`](../reference/scripts.md#diagnose-app) driver
script to identify possible issues before starting a session. It checks manifest permissions,
obfuscation, the Jetpack Compose version (the only library whose version still has to match the
application's), and other static preconditions for precompile.
Any problems cause the script to exit with a non-zero code.

## Jetpack Compose

For handling issues when testing Jetpack Compose apps (or hybrid View + Compose UIs), refer to the
[Compose Troubleshooting guide](./compose.md).

## Session Startup

For issues arising on session startup in non-Compose apps, refer to the
[Session Startup Troubleshooting guide](./startup.md).

## Class Conflicts

The Espresso server moves its bundled libraries to a private package so they do not conflict with the
ones of the application under test. For the related errors and settings, refer to the
[Class Conflicts guide](./class-conflicts.md).

## Apps Without Compose

By default, the driver builds the Espresso server with Jetpack Compose dependencies included. This
may cause problems if the application under test has no Compose dependencies.

In such cases, it may help to exclude the Compose dependencies in the Espresso server. This can be
done by setting the [`composeSupport`](../reference/capabilities.md#composesupport) property of the
[`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig) capability to
`false`. This also moves the Kotlin standard library and the other libraries that would otherwise be
shared with the application, see [Class Conflicts](./class-conflicts.md).

While in a session without Compose dependencies, make sure to avoid using the
[`driver`](../reference/settings.md#driver) setting, and leave it at its default `espresso` value.
