---
title: Jetpack Compose Issues
---

The Espresso driver runs the application under test (AUT) under Android instrumentation. The
APKs of the Espresso server and the AUT share a single process and a merged classpath. This design
is powerful, but it also means Compose-related failures often look like generic "server failed to
start" errors, while the real cause is a dependency or manifest mismatch between the two APKs.

This guide collects problems that commonly appear when testing Compose-heavy (or hybrid View +
Compose) apps, especially when using a dedicated fixture such as [`compose-playground`](https://github.com/appium/compose-playground).

For general driver issues (signatures, `forceEspressoRebuild`, ProGuard), see
[the main Troubleshooting guide](./index.md).

## Before Debugging

1. Align Compose versions between the AUT and the Espresso server that the driver builds for that
   session, by passing [`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig)
   with a `toolsVersions.composeVersion` that matches the Compose UI version of the AUT (see
   [Matching Compose Versions](#matching-compose-versions)). If the installed driver version is
   already new enough, this alignment may not be necessary.
2. Set the compile SDK high enough for the AUT Compose stack (Compose 1.11.x needs `35` or higher
   on the `server` build). This can be done using the `toolsVersions.compileSdk` key in the same
   `appium:espressoBuildConfig` capability. If this is omitted, the driver uses the default
   driver-defined `compileSdk` version.
3. Rebuild the server after changing the build config: set `appium:forceEspressoRebuild` to `true`
   for one session, then set it back to `false`.
4. Reinstall the AUT after manifest or permission changes (or bump its `versionCode`). An old
   install may lack `INTERNET` or other fixes, even if the APK was locally rebuild.
5. Collect logcat for the AUT process (`io.appium.composeplayground`, etc.) and filter for
   `TestRunner`, `AndroidRuntime`, and `appium`.

## Matching Compose Versions

The Espresso server only explicitly declares the `androidx-compose-ui-test` and
`androidx-compose-ui-test-junit4` libraries; those artifacts pull in Compose runtime classes that
must be compatible with the AUT's Compose UI. There are a few ways to try and match these
dependencies with the AUT:

| <div style="width:6em">Approach</div> | When to use |
| --- | --- |
| Install a newer Espresso driver | Preferred when the driver defaults already match the AUT (refer to the [`toolsVersions` table](../reference/capabilities.md#toolsversions) for bundled Compose and `compileSdk`) |
| Set `toolsVersions` inside `appium:espressoBuildConfig` | The AUT uses newer Compose or needs a higher `compileSdk` than the installed driver defaults |
| Set `appium:forceEspressoRebuild` to `true` | After changing `espressoBuildConfig`, or when debugging stale server APKs |

Example capabilities (Node.js / WebdriverIO style):

```javascript
{
  'appium:automationName': 'Espresso',
  'appium:app': '/path/to/your-app.apk',
  'appium:espressoBuildConfig': JSON.stringify({
    toolsVersions: {
      compileSdk: '35',
      composeVersion: '1.11.2',
    },
  }),
  'appium:forceEspressoRebuild': true,
}
```

The `appium:espressoBuildConfig` capability can also be pointed to a JSON file path on the Appium
host instead.

If versions are misaligned, errors like `NoSuchMethodError` may appear, referencing
`androidx.compose.runtime.ComposerKt` or other Compose classes. The stack trace often points at
`io.appium.espressoserver.test`, even though the AUT triggered the failing code path.

## Hybrid Apps (Views + Compose)

If working with an app that combines standard Android Views with Compose fixtures, e.g. a native
menu (`RecyclerView` / `TextView`) with Compose demos inside a `ComposeView`, make sure to use the
[`driver`](../reference/settings.md#driver) setting to set the required subdriver:

```js
// JavaScript (WebdriverIO)
await driver.updateSettings({ driver: 'espresso' });
await driver.$("//*[@text='Clickable Component']").click();
await driver.updateSettings({ driver: 'compose' });
```

Ensure menu labels and the demo content match what is expected by tests. For examples, refer to the
[`test/functional/commands/jetpack-compose-*.ts`](https://github.com/appium/appium-espresso-driver/tree/master/test/functional/commands)
tests in the driver repository. The tests use Appium's own [`compose-playground`](https://github.com/appium/compose-playground)
app, which also documents the contract in its README.

## Possible Issues and Fixes

### Server startup failure due to `INTERNET` permission

**Symptoms**

- Error messages for `Espresso server has failed to start`, specifying the `INTERNET` permissions
- Session creation fails with a message to add `<uses-permission android:name="android.permission.INTERNET" />`
  to the AUT manifest
- Logcat may show `java.net.SocketException: socket failed: EPERM (Operation not permitted)` wrapped as
  `IllegalStateException` about `INTERNET` in `ServerBase.kt`

**Cause**

The Espresso HTTP server binds a local socket. Android requires the application under test to
declare `INTERNET`, not only the instrumentation package.

**Fixes**

Add the following line to the `AndroidManifest.xml` of the app under test:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Reinstall the app (`adb uninstall <package>` then install the new APK). Verify with:

```bash
adb shell dumpsys package <your.package> | grep INTERNET
```

Install permissions should now list `android.permission.INTERNET: granted=true`.

### `Resources$NotFoundException`

**Symptoms**

- Error messages such as `Resources$NotFoundException: String resource ID #0x7f…`
- Instrumentation exits before tests run
- Logcat: `Unable to get provider androidx.startup.InitializationProvider` and
  `Resources$NotFoundException` inside `AppInitializer.discoverAndInitialize`
- Resource ID often corresponds to `androidx_startup` from AndroidX App Startup

**Cause**

Under instrumentation, manifest entries from the Espresso test APK and the AUT are merged.
`InitializationProvider` metadata can reference a string resource ID that was compiled in the
Espresso server APK's `R` table but resolved against the AUT resources (or the reverse), so
`getString()` fails. See [this issue](https://github.com/appium/appium-espresso-driver/issues/911)
for more details.

**Fixes**

In the AUT manifest, remove the startup provider for the test process (adjust package as needed):

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application>
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            tools:node="remove" />
    </application>
</manifest>
```

Optionally add `androidx.startup:startup-runtime` to the AUT, so `androidx_startup` exists when the
provider is not removed. For Espresso fixtures, removing the provider is usually the more reliable
fix.

### `addOnConfigurationChangedListener` Not Found

**Symptoms**

- `NoSuchMethodError` messages specifying `FragmentActivity.addOnConfigurationChangedListener`
- Session starts, then crashes when the main activity launches
- Logcat: `No virtual method addOnConfigurationChangedListener(Landroidx/core/util/Consumer;)V in class Landroidx/fragment/app/FragmentActivity` (class loaded from the AUT APK)

**Cause**

The AUT bundles an older `androidx.fragment` / `AppCompatActivity` while newer `androidx.activity`
(or Espresso's merged classpath) expects a newer `FragmentActivity` API.

**Fixes**

- Prefer `ComponentActivity` for hybrid Compose + View screens, when AppCompat widgets are not
  needed, *or*
- Align `appcompat`, `activity`, and `fragment` versions (use a current Compose BOM and avoid
  pinning very old AppCompat with very new `activity-compose`)

### `ComposerKt.*` Not Found

**Symptoms**

- `NoSuchMethodError` messages specifying `ComposerKt.isTraceInProgress` or other `ComposerKt`
  methods
- Session or first Compose interaction crashes
- Message mentions `androidx.compose.runtime.ComposerKt` and a missing static or virtual method
- Often references `io.appium.espressoserver.test` in the dex path

**Cause**

Compose runtime version skew: the AUT was built with a newer Compose BOM than the Espresso server's
`ui-test` artifacts (or the opposite).

**Fixes**

1. Upgrade the installed Espresso driver package if a newer release already bundles a matching
   Compose / `compileSdk`.
2. Otherwise set `appium:espressoBuildConfig` so `toolsVersions.composeVersion` matches the AUT's
   `androidx.compose.ui` version (app developers can obtain this from the app's Gradle dependency
   report).
3. When using Compose 1.11.x, set `toolsVersions.compileSdk` to `35` or higher in the same
   `espressoBuildConfig`.
4. Start a session with `appium:forceEspressoRebuild`: `true`. If problems persist, uninstall
   `io.appium.espressoserver.test` and the AUT package from the device, then create a new session.

Do _not_ downgrade the AUT's Compose stack unless no other option exists; align the Espresso server
build (via capabilities or a newer driver) to the app instead.

### Permission Denial Due to Signature Mismatch

**Symptoms**

- Logcat: `package io.appium.espressoserver.test does not have a signature matching the target <aut.package>`
- Instrumentation never starts; session fails quickly

**Cause**

Espresso requires the AUT and `androidTest` APK to be signed compatibly. Reinstalling only the AUT
(or only the test APK) with a different debug keystore breaks the pair.

**Fixes**

1. Remove the mismatched pair and let Appium reinstall both APKs with a matching signature:

    ```bash
    adb uninstall io.appium.espressoserver.test
    adb uninstall <your.aut.package>
    ```

    Create a new session with `appium:forceEspressoRebuild: true` so Appium rebuilds and reinstalls the
    server against the current AUT.

2. If the AUT is already signed separately (release build, `appium:noReset: true`, or a manually
   installed APK), configure [App Signing capabilities](../reference/capabilities.md#app-signing)
   so that both the AUT and the Espresso server are signed the same way.
   
    This can be done by setting `appium:useKeystore` to `true`, plus pointing `appium:keystorePath`,
    `appium:keystorePassword`, `appium:keyAlias`, and `appium:keyPassword` to the keystore that
    matches the AUT. The driver then signs the rebuilt `io.appium.espressoserver.test` APK with
    that keystore as well.
    
    Do not use `appium:noSign: true` unless the server APK is already signed with the same
    certificate as the AUT.

### Proxy Failure or Crash Upon Click

**Symptoms**

- Errors such as `Could not proxy command` / `instrumentation process has crashed` after a click
- Menu navigation (Espresso / native) works; crash happens when opening a Compose screen or
  clicking a Compose node
- Often follows a `NoSuchMethodError` in logcat (see Compose version and `addOnConfigurationChangedListener`
  sections above).

**Fixes**

Treat this as a classpath crash: read the first `Caused by:` in logcat, then apply fixes from the
matching section above. Version alignment fixes most post-click crashes.

### Some Locator Strategies Do Not Work

**Symptoms**

- Strategies like `tag name` / `accessibility id` work fine
- XPaths like `//*[@view-tag='lol']//*[@content-desc='desc']` do not work

**Cause**

Compose semantics appear as a tree. XPath over page source expects parent/child relationships.
Putting `testTag` and `contentDescription` on the same modifier produces a single node with both
attributes, so a descendant XPath will not match.

**Fixes**

Structure the AUT UI to generate the semantics tree the tests expect, for example:

- `testTag("lol")` on a parent `Box` with `clickable`
- `contentDescription = "desc"` on a child `Text`
- Keep `clickable` on the parent if attribute tests expect `clickable: false` on the `Text` node
  found by visible text

Enable `testTagsAsResourceId` for Espresso interop when using tag-based locators:

```kotlin
Modifier.semantics { testTagsAsResourceId = true }
```

There is also a reference implementation in the `compose-playground` test app in
[`ClickableDemo.kt`](https://github.com/appium/compose-playground/blob/main/app/src/main/java/io/appium/composeplayground/compose/ClickableDemo.kt).

### `ProcessLifecycleOwner` Classpath Conflicts

**Symptoms**

- Crash at startup with `NoSuchMethodError: ProcessLifecycleOwner.init(Context)` or similar
  lifecycle API mismatches
- Often appear after pinning old `lifecycle-extensions` via `espressoBuildConfig.additionalAndroidTestDependencies`

**Cause**

Legacy lifecycle artifacts in the Espresso server build conflicting with modern Compose / lifecycle
versions in the AUT.

**Fixes**

- Remove unnecessary `lifecycle-extensions` (and similar) pins from `additionalAndroidTestDependencies`
- Align lifecycle artifacts with the AUT (e.g. `lifecycle-runtime-ktx` from the same BOM era as
  Compose)
- Use a modern fixture APK that does not embed obsolete `ProcessLifecycleOwnerInitializer` unless
  required

## Related Links

- [Troubleshooting](./index.md) — general Espresso driver issues
- [Activity Startup Issues](./activity-startup.md)
- [`appium:espressoBuildConfig`](../reference/capabilities.md#espressobuildconfig) —
  `composeVersion`, `compileSdk`, dependencies
- [`compose-playground`](https://github.com/appium/compose-playground) — maintained AUT for Compose
  e2e tests
- [Issue #812](https://github.com/appium/appium-espresso-driver/issues/812) — classpath /
  dependency alignment
- [Issue #911](https://github.com/appium/appium-espresso-driver/issues/911) —
  `Resources$NotFoundException` / App Startup
