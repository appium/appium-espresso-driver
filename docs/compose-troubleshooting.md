## How To Troubleshoot Jetpack Compose Apps

Espresso driver runs your application under **Android instrumentation**. The Espresso server test APK and your application APK share a single process and a merged classpath. That design is powerful, but it also means Compose-related failures often look like generic “server failed to start” errors while the real cause is a dependency or manifest mismatch between the two APKs.

This guide collects problems that commonly appear when testing **Compose-heavy** (or hybrid View + Compose) apps, especially when using a dedicated fixture such as [compose-playground](https://github.com/appium/compose-playground). For general driver issues (signatures, `forceEspressoRebuild`, ProGuard), see [Troubleshooting](../README.md#troubleshooting) in the README.

### Before You Debug

Everything below is configured from your **test client** (Java, Node.js, Python, etc.) through [W3C capabilities](../README.md#espresso-build-config). You do not edit files inside the installed `appium-espresso-driver` package unless you are developing the driver itself.

1. **Align Compose versions** between your AUT and the Espresso server that the driver builds for that session. If the driver version you installed is already new enough, you may not need any override. Otherwise pass [`appium:espressoBuildConfig`](../README.md#espresso-build-config) with a `toolsVersions.composeVersion` that matches your app’s Compose UI version (see [Matching Compose Versions](#matching-compose-versions)).
2. **Set `compileSdk` high enough** for your Compose stack (Compose 1.11.x needs **35** or higher on the **server** build). Pass it in the same capability: `toolsVersions.compileSdk` inside `appium:espressoBuildConfig`. If you omit it, the driver uses the `compileSdk` baked into the driver version you installed.
3. **Rebuild the server after changing build config**: set `appium:forceEspressoRebuild` to `true` for one session, then set it back to `false`.
4. **Reinstall the AUT** after manifest or permission changes (or bump `versionCode`). An old install may lack `INTERNET` or other fixes even though you rebuilt the APK locally.
5. **Collect logcat** for the AUT process (`io.appium.composeplayground`, etc.) and filter for `TestRunner`, `AndroidRuntime`, and `appium`.

---

### Matching Compose Versions

The Espresso server only declares **`ui-test`** and **`ui-test-junit4`** explicitly; those artifacts pull in Compose runtime classes that must be compatible with the AUT’s Compose UI.

| What you can do (client API) | When to use |
| --- | --- |
| Install a newer **appium-espresso-driver** release | Preferred when defaults already match your AUT (check the driver changelog / release notes for bundled Compose and `compileSdk`). |
| **`appium:espressoBuildConfig`** with `toolsVersions` | Your AUT uses newer Compose or needs a higher `compileSdk` than the installed driver defaults. |
| **`appium:forceEspressoRebuild`: `true`** | After changing `espressoBuildConfig`, or when debugging stale server APKs. |

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

You can also point `appium:espressoBuildConfig` at a JSON file path on the Appium host instead of an inline string. Supported keys under `toolsVersions` are listed in [Espresso Build Config](../README.md#toolsversions) (`composeVersion`, `compileSdk`, `kotlin`, etc.).

If versions are misaligned, you may see `NoSuchMethodError` referencing `androidx.compose.runtime.ComposerKt` or other Compose classes. The stack trace often points at `io.appium.espressoserver.test` even though the AUT triggered the failing code path.

---

### Typical Errors and Fixes

#### `Espresso server has failed to start` — `INTERNET` permission

**Symptoms**

- Session creation fails with a message to add `<uses-permission android:name="android.permission.INTERNET" />` to the AUT manifest.
- Logcat may show `java.net.SocketException: socket failed: EPERM (Operation not permitted)` wrapped as `IllegalStateException` about `INTERNET` in `ServerBase.kt`.

**Cause**

The Espresso HTTP server binds a local socket. Android requires the **application under test** to declare `INTERNET`, not only the instrumentation package.

**Fix**

Add to the AUT `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Reinstall the app (`adb uninstall <package>` then install the new APK). Verify with:

```bash
adb shell dumpsys package <your.package> | grep INTERNET
```

You should see `android.permission.INTERNET: granted=true` under install permissions.

---

#### `Resources$NotFoundException: String resource ID #0x7f…`

**Symptoms**

- Instrumentation exits before tests run.
- Logcat: `Unable to get provider androidx.startup.InitializationProvider` and `Resources$NotFoundException` inside `AppInitializer.discoverAndInitialize`.
- Resource id often corresponds to `androidx_startup` from AndroidX App Startup.

**Cause**

Under instrumentation, manifest entries from the Espresso test APK and the AUT are merged. `InitializationProvider` metadata can reference a string resource id that was compiled in the **test** APK’s `R` table but resolved against the **AUT** resources (or the reverse), so `getString()` fails. See [appium-espresso-driver#911](https://github.com/appium/appium-espresso-driver/issues/911).

**Fix**

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

Optionally add `androidx.startup:startup-runtime` to the AUT so `androidx_startup` exists when you do **not** remove the provider. For Espresso fixtures, removing the provider is usually the more reliable fix.

---

#### `NoSuchMethodError` — `FragmentActivity.addOnConfigurationChangedListener`

**Symptoms**

- Session starts, then crashes when the main activity launches.
- Logcat: `No virtual method addOnConfigurationChangedListener(Landroidx/core/util/Consumer;)V in class Landroidx/fragment/app/FragmentActivity` (class loaded from the AUT APK).

**Cause**

The AUT bundles an older `androidx.fragment` / `AppCompatActivity` while newer `androidx.activity` (or Espresso’s merged classpath) expects a newer `FragmentActivity` API.

**Fix**

- Prefer **`ComponentActivity`** for hybrid Compose + View screens when you do not need AppCompat widgets, **or**
- Align `appcompat`, `activity`, and `fragment` versions (use a current Compose BOM and avoid pinning very old AppCompat with very new `activity-compose`).

---

#### `NoSuchMethodError` — `ComposerKt.isTraceInProgress` (or other `ComposerKt` methods)

**Symptoms**

- Session or first Compose interaction crashes.
- Message mentions `androidx.compose.runtime.ComposerKt` and a missing static or virtual method.
- Often references `io.appium.espressoserver.test` in the dex path.

**Cause**

**Compose runtime version skew**: the AUT was built with a newer Compose BOM than the Espresso server’s `ui-test` artifacts (or the opposite).

**Fix**

1. **Upgrade** the installed `appium-espresso-driver` package if a newer release already bundles a matching Compose / `compileSdk`.
2. Otherwise set **`appium:espressoBuildConfig`** so `toolsVersions.composeVersion` matches your AUT’s `androidx.compose.ui` version (your app team can read this from the app’s Gradle dependency report).
3. When using Compose **1.11.x**, include **`toolsVersions.compileSdk`: `"35"`** (or higher) in the same `espressoBuildConfig`.
4. Start a session with **`appium:forceEspressoRebuild`: `true`**. If problems persist, uninstall `io.appium.espressoserver.test` and your AUT package from the device, then create a new session.

Do **not** downgrade the AUT’s Compose stack unless you have no other option; align the **server** build (via capabilities or a newer driver) to the app instead.

---

#### `Permission Denial` — signatures do not match

**Symptoms**

- Logcat: `package io.appium.espressoserver.test does not have a signature matching the target <aut.package>`.
- Instrumentation never starts; session fails quickly.

**Cause**

Espresso requires the AUT and `androidTest` APK to be signed compatibly. Reinstalling only the AUT (or only the test APK) with a different debug keystore breaks the pair.

**Fix**

```bash
adb uninstall io.appium.espressoserver.test
adb uninstall <your.aut.package>
```

Create a new session with `appium:forceEspressoRebuild: true` so Appium rebuilds and reinstalls the server against the current AUT.

---

#### `Could not proxy command` / `instrumentation process has crashed` after a click

**Symptoms**

- Menu navigation (Espresso / native) works; crash happens when opening a Compose screen or clicking a Compose node.
- Often follows a `NoSuchMethodError` in logcat (see Compose version and `FragmentActivity` sections above).

**Fix**

Treat as a classpath crash: read the **first** `Caused by:` in logcat, then apply the matching section above. Version alignment fixes most post-click crashes.

---

#### Compose locators work in one form but not another

**Symptoms**

- `driver: 'compose'` + `tag name` / `accessibility id` work.
- XPath like `//*[@view-tag='lol']//*[@content-desc='desc']` does not.

**Cause**

Compose semantics appear as a **tree**. XPath over page source expects parent/child relationships. Putting `testTag` and `contentDescription` on the **same** modifier produces a single node with both attributes, so a descendant XPath will not match.

**Fix**

Structure the UI for the semantics tree the tests expect, for example:

- `testTag("lol")` on a parent `Box` with `clickable`.
- `contentDescription = "desc"` on a child `Text`.
- Keep `clickable` on the parent if attribute tests expect `clickable: false` on the `Text` node found by visible text.

Enable `testTagsAsResourceId` for Espresso interop when using tag-based locators:

```kotlin
Modifier.semantics { testTagsAsResourceId = true }
```

Reference implementation: [compose-playground `ClickableDemo.kt`](https://github.com/appium/compose-playground/blob/main/app/src/main/java/io/appium/composeplayground/compose/ClickableDemo.kt).

---

#### `ProcessLifecycleOwner` / lifecycle classpath conflicts

**Symptoms**

- Crash at startup with `NoSuchMethodError: ProcessLifecycleOwner.init(Context)` or similar lifecycle API mismatches.
- Often after pinning old `lifecycle-extensions` via `espressoBuildConfig.additionalAndroidTestDependencies`.

**Cause**

Legacy lifecycle artifacts in the **server** build conflicting with modern Compose / lifecycle versions in the AUT.

**Fix**

- Remove unnecessary `lifecycle-extensions` (and similar) pins from `additionalAndroidTestDependencies`.
- Align lifecycle artifacts with the AUT (e.g. `lifecycle-runtime-ktx` from the same BOM era as Compose).
- Use a modern fixture APK that does not embed obsolete `ProcessLifecycleOwnerInitializer` unless required.

---

### Hybrid apps (Views + Compose)

Many fixtures use a **native menu** (RecyclerView / TextView) and **Compose demos** inside a `ComposeView`. Tests switch subdrivers explicitly:

```js
await driver.updateSettings({ driver: 'espresso' });
await driver.$("//*[@text='Clickable Component']").click();
await driver.updateSettings({ driver: 'compose' });
```

Ensure menu labels and demo content match what your tests expect. The compose-playground app documents the contract in its README and mirrors the e2e specs in this repo under `test/functional/commands/jetpack-compose-*.ts`.

---

### Disabling Compose in the server build

If the AUT has **no** Compose dependencies, embedding Compose in the server can still cause conflicts. From the client:

```javascript
'appium:espressoBuildConfig': JSON.stringify({ composeSupport: false }),
'appium:forceEspressoRebuild': true,
```

See [Jetpack Compose Support](../README.md#jetpack-compose-support) and `composeSupport` in [Espresso Build Config](../README.md#espresso-build-config). Do not set `driver: 'compose'` when support is disabled.

---

### Related links

- [Troubleshooting](../README.md#troubleshooting) — general Espresso driver issues
- [Espresso Build Config](../README.md#espresso-build-config) — `composeVersion`, `compileSdk`, dependencies
- [How To Troubleshoot Activities Startup](activity-startup.md)
- [compose-playground](https://github.com/appium/compose-playground) — maintained AUT for Compose e2e tests
- [Issue #812](https://github.com/appium/appium-espresso-driver/issues/812) — classpath / dependency alignment
- [Issue #911](https://github.com/appium/appium-espresso-driver/issues/911) — `Resources$NotFoundException` / App Startup
