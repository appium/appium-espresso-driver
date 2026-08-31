---
hide:
  - navigation
  - toc

title: Contributing
---

Contributions to this project are welcome! To start off, clone it from GitHub and run:

```bash
npm install
```

To build the project:

```bash
npm run build        # driver and Espresso server
npm run build:server # Espresso server only
npm run build:node   # driver only
```

The Espresso server can also be built through Android Studio, or via `gradle`:

```bash
./gradlew clean assembleDebug assembleAndroidTest                          # without a targeted app under test
./gradlew -PappiumTargetPackage=io.appium.android.apis assembleAndroidTest # for targeting a specific app
```

To run in development mode:

```bash
npm run dev
```

To run tests for the driver part of the project:

```bash
npm run test     # unit 
npm run e2e-test # functional
```

!!! tip

    If running functional tests, it is often useful to be able to set break points in the Espresso
    server.

    To accomplish this, open the Espresso server project in Android Studio, set the
    [`ESPRESSO_JAVA_DEBUG`](./reference/env-vars.md#espresso_java_debug) environment variable, and then
    run the tests. When the tests start repeatedly pinging the Espresso server (i.e. outputting
    `Proxying [GET /status] to [GET http://localhost:4567/status] with no body`), click on
    `Attach debugger to Android process` in Android Studio.

To run tests for the Espresso server, use Android Studio. Tests are located at
`espresso-server/library/src/test/java/io/appium/espressoserver/test`.

There are also a number of environment variables that can be used when running the tests locally.
These include:

* `APPIUM_TEST_SERVER_HOST` - set the host URL (default `127.0.0.1`)
* `APPIUM_TEST_SERVER_PORT` - set the host port (default `4567`)

To lint and format:

```bash
npm run lint:fix
npm run format
```

To develop documentation:

```bash
npm run install-docs-deps # install the dependencies (Python packages)
npm run dev:docs          # serve the docs locally and watch for changes
```

Additional scripts can also be found in `package.json`.
