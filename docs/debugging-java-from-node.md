## Debugging Java from NodeJS Context

When running functional tests from NodeJS it is often helpful to be able to set break points in the Java server.

To accomplish this, have the espresso-server project (/espresso-server) open in Android Studio; set the env variable ESPRESSO_JAVA_DEBUG to true and then run the Mocha tests. When it starts repeatedly pinging the Java server (ie: it does this over and over `Proxying [GET /status] to [GET http://localhost:8080/status] with no body`) click on the `Attach debugger to Android process` in Android Studio.