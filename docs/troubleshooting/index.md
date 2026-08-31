---
title: Troubleshooting
---

As the Espresso driver is a grey-box driver with tighter coupling to the app under test (AUT), it
is fairly common to run into one or more issues. Usually, problems manifest immediately on session
startup, and may have many different causes.

It is generally recommended to use the [`diagnose-app`](../reference/scripts.md#diagnose-app) driver
script to identify possible issues before starting a session. It checks manifest permissions,
obfuscation, AndroidX/Compose dependency alignment, and other static preconditions for precompile.
Any problems cause the script to exit with a non-zero code.

## Jetpack Compose

For handling issues when testing Jetpack Compose apps (or hybrid View + Compose UIs), refer to the
[Compose Troubleshooting guide](./compose.md).

## Session Startup

For issues arising on session startup in non-Compose apps, refer to the
[Session Startup Troubleshooting guide](./startup.md).

## Apps Without Compose

By default, the driver builds the Espresso server with Jetpack Compose dependencies included. This
may cause problems if the application under test has no Compose dependencies.

In such cases, it may help to exclude the Compose dependencies in the Espresso server. This can be
done by setting the [`composeSupport`](../reference/capabilities.md#composesupport) property of the
[`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig) capability to
`false`.

While in a session without Compose dependencies, make sure to avoid using the
[`driver`](../reference/settings.md#driver) setting, and leave it at its default `espresso` value.
