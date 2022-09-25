## Hybrid Mode

Espresso driver supports automation of hybrid apps that use Chrome-based web views, by managing a
[Chromedriver](https://sites.google.com/a/chromium.org/chromedriver/) instance and
proxying commands to it when necessary.

The following endpoints are used to control the current context:

- POST `/session/:sessionId/context`: To set the current context. The body contains a single mandatory `name` parameter, which has the name of the context to be set. The name of the default context is `NATIVE_APP`.
- GET `/session/:sessionId/context`: To retrieve the name of the current context
- GET `/session/:sessionId/contexts`: To retrieve the list of available context names
- [mobile: getContexts](#mobile-getcontexts)

By default, the driver starts in the native context, which means that most of REST API commands are being
forwarded to the downstream Espresso server.
This server is running on the device under test, and transforms API commands to appropriate low-level Espresso framework calls. There is always only one native context, although multiple web contexts are possible.
Each web context could contain zero or more pages/windows.

Web context(s) could be detected if a web view is active on the device. If a context is switched to
a web one then Espresso driver spins up a Chromedriver instance for it and forwards most of the commands
to that Chromedriver instance. Note that web views must be properly configured and
debuggable in order to connect to them or get their names in the list of available contexts.
The availability of a particular web view could be easily verified by using
[Chrome Remote Debugger](https://developer.chrome.com/docs/devtools/remote-debugging/).
You could switch between different contexts (and windows in them) at any time during the session.

The [appium-chromedriver](https://github.com/appium/appium-chromedriver) package bundled with Espresso driver always
tries to download the most recent version of Chromedriver known to it. Google requires that the used Chromedriver version must always match to the version of the a web view engine version being automated. If these versions do not match then Chromedriver fails its creation, and context switch API shows a failure message
similar to:

```
An unknown server-side error occurred while processing the command.
Original error: unknown error: Chrome version must be >= 55.0.2883.0
```

To work around this issue it is necessary to provide Espresso driver with a proper Chromedriver binary
that matches to the Chrome engine version running on the device under test.
Read the [Chromedriver/Chrome compatibility](#chromedriverchrome-compatibility) topic below to
know more about finding a matching Chromedriver executable.

There are several ways to provide a customized Chromedriver to Espresso driver:

#### When installing the driver

Specify the Chromedriver version in the `CHROMEDRIVER_VERSION` environment variable:

```bash
CHROMEDRIVER_VERSION=2.20 appium install driver uiautomator2
```

#### When starting a session (manual discovery)

Chromedriver version can be specified in session capabilities, by providing the
`appium:chromedriverExecutable` [capability](#web-context),
containing the full path to a matching Chromedriver executable, which must be manually
downloaded and put to the server file system.

#### When starting a session (automated discovery)

Espresso driver could also try to detect the version of the target Chrome engine and
download matching Chromedriver for it automatically if it does not exist on the local file system.
Read the [Automatic discovery of compatible Chromedriver](#automatic-discovery-of-compatible-chromedriver)
topic below for more details.

### Chromedriver/Chrome Compatibility

Since version *2.46* Google has changed their rules for Chromedriver versioning, so now the major Chromedriver version corresponds to the major web view version, that it can automate. Follow the [Version Selection](https://chromedriver.chromium.org/downloads/version-selection) document in order to manually find the Chromedriver, that supports your current web view if its major version is equal or above *73*.

To find the minimum supported browsers for older Chromedriver versions (below *73*), get the
[Chromium](https://www.chromium.org/Home)
[source code](https://chromium.googlesource.com/chromium/src/+/master/docs/get_the_code.md),
check out the release commit, and check the variable `kMinimumSupportedChromeVersion`
in the file `src/chrome/test/chromedriver/chrome/version.cc`. (To find the
release commits, you could use `git log --pretty=format:'%h | %s%d' | grep -i "Release Chromedriver version"`.)

The complete list of available Chromedriver releases and release notes is located at [Chromedriver Storage](https://chromedriver.storage.googleapis.com/index.html).

The list of Chromedriver versions and their matching minimum
Chrome versions known to appium-chromedriver package is stored at
https://raw.githubusercontent.com/appium/appium-chromedriver/master/config/mapping.json

### Automatic Discovery of Compatible Chromedriver

Espresso driver is able to pick the correct Chromedriver for the
version of Chrome/web view under test. While appium-chromedriver only comes bundled with the Chromedriver
most recently released at the time of the corresponding package version's release, more Chromedriver
versions could be downloaded and placed into a custom location indicated to Espresso driver via the `appium:chromedriverExecutableDir` capability.

A custom mapping of Chromedrivers to the minimum
Chrome/web view version they support could be given to Espresso driver through the
`appium:chromedriverChromeMappingFile` capability. This should be the
absolute path to a file with the mapping
in it. The contents of the file needs to be parsable as a JSON object, like:

```json
{
  "2.42": "63.0.3239",
  "2.41": "62.0.3202"
}
```

There is a possibility to automatically download the necessary chromedriver(s) into `appium:chromedriverExecutableDir` from the official Google storage. The script will automatically search for the newest chromedriver version that supports the given web view, download it (the hash sum is verified as well for the downloaded archive) and add to the `appium:chromedriverChromeMappingFile` mapping. Everything, which is needed to be done from your side is to execute the server with `chromedriver_autodownload` feature enabled (like `appium server --allow-insecure chromedriver_autodownload`).

### Troubleshooting Chromedriver Download Issues

When Espresso driver is installed it automatically downloads Chromedriver, so there is a possibility
of network or other issues leading to an installation failure.

By default, Chromedriver is retrieved from `https://chromedriver.storage.googleapis.com/`.
To use a mirror of the above URL change the value of `CHROMEDRIVER_CDNURL` environemnt variable:

```bash
CHROMEDRIVER_CDNURL=https://npmmirror.com/mirrors/chromedriver appium driver install uiautomator2
```

It may also be necessary to adjust network proxy and firewall settings for the above to work.

In case you would like skip the download of Chromedriver entirely, do it by
defining the `APPIUM_SKIP_CHROMEDRIVER_INSTALL` environment variable:

```bash
APPIUM_SKIP_CHROMEDRIVER_INSTALL=1 appium driver install uiautomator2
```

### W3C Support in Web Context

Chromedriver did not follow the W3C standard until version 75. If you encounter proxy command error like [this issue](https://github.com/appium/python-client/issues/234), please update your Chromedriver version.
Old Android devices can't use newer Chromedriver versions. You could avoid the error if you enforce
Mobile JSON Wire Protocol for Chromedriver. This could be done by providing `{'w3c': False}` item
to `appium:chromeOptions` capability value.
Since major version *75* W3C mode is the default one for Chromedriver, although it could be still switched to JSONWP one as described above (keep in mind that eventually Chromedriver will drop the support of
JSON Wire protocol completely).
The history of W3C support in Chromedriver is available for reading at
[downloads section](https://sites.google.com/a/chromium.org/chromedriver/downloads).