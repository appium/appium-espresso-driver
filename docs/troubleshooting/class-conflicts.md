---
title: Class Conflicts
---

The Espresso server runs in the same process, and in the same class loader, as the application under
test (AUT). Every library bundled with the server (AndroidX, Kotlin, Hamcrest, JUnit, ...) used to
shadow the copy of the same library in the AUT, which caused version conflicts such as
`NoSuchMethodError` in the AUT code.

## How it works

By default the server is built with [`shadeServer`](../reference/capabilities.md#shadeserver) enabled:

* the classes bundled with the server are moved to the `io.appium.espressoserver.shaded` package, so
  they cannot clash with the ones of the AUT;
* Jetpack Compose and its dependencies (`androidx.compose.*`, the Kotlin standard library,
  `kotlinx.*`, and everything they reference) are **not** moved, because Compose test code needs the
  Compose classes of the AUT. Their versions must still match the AUT's ones,
  see [Compose troubleshooting](./compose.md). Building the server with
  [`composeSupport`](../reference/capabilities.md#composesupport) set to `false` moves them as well;
* the classes of the libraries listed in `additionalAppDependencies` and
  `additionalAndroidTestDependencies`, and the classes they reference, are not moved either. These
  options keep working as before, but pinning the AUT's library versions is usually not needed
  anymore: try removing the pins first. The remaining use cases are Compose version matching and
  libraries with native code (declare them in `additionalAndroidTestDependencies`);
* a few server classes keep their names because the platform or the AUT refers to them by name
  (the instrumentation runner, `IdlingResource`).

Classes of the moved libraries show up in stack traces with the `io.appium.espressoserver.shaded.`
prefix. The prefix is removed from the messages the server returns to the client.

## Symptoms and settings

Even with `shadeServer` enabled some issues are still possible, such as the following ones.
The driver adds a hint with a link to the matching one of the first four to the session startup error
when it recognizes it in the instrumentation output or in the crash buffer of the device.

<a id="illegal-access-shaded"></a>

### IllegalAccessError

`IllegalAccessError` with `io.appium.espressoserver.shaded` classes is a relocation problem in the
server build. Report a bug, and set `shadeServer` to `false` meanwhile.

<a id="service-loader"></a>

### ServiceLoader

`Exception handler was not found via a ServiceLoader` means that a service file was not relocated.
Report a bug, and set `shadeServer` to `false` meanwhile.

<a id="startup-initializer"></a>

### StartupException

`StartupException` with a `ClassCastException` mentioning `Initializer`, or `ViewTreeLifecycleOwner`
errors, mean that the classes shared with the AUT are not consistent. Report a bug, and set
`shadeServer` to `false` meanwhile.

<a id="compose-version"></a>

### Compose version mismatch

`NoSuchMethodError` or `NoClassDefFoundError` mentioning `androidx.compose` means that the Compose
version of the AUT differs from the one of the server. Set `toolsVersions.composeVersion` to the AUT's
version. If the AUT has no Compose, set `composeSupport` to `false`.

### No compose hierarchies found

The current screen has no Compose content. Switch the [`driver`](../reference/settings.md#driver)
setting back to `espresso`, or navigate to a Compose screen.

## Shading disabled

If the server is built with `shadeServer` disabled, conflicts like `NoSuchMethodError`,
`NoClassDefFoundError`, `IncompatibleClassChangeError` or `Resources$NotFoundException` in the AUT
code with the Espresso server classes in the stack trace are expected. Enable shading again
(that is the default) and remove the library pins from `additionalAppDependencies`.

## Changing the settings

Use the [`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig)
capability, and add `appium:forceEspressoRebuild` for the first session after the change:

```json
{
  "appium:forceEspressoRebuild": true,
  "appium:espressoBuildConfig": {"shadeServer": false}
}
```

## Reporting a bug

Please attach the following to the issue:

* the crash buffer of the device (`adb logcat -d -b crash`);
* the `appium:espressoBuildConfig` value;
* the version of Compose the AUT uses, if it has any.
