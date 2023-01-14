## Parallel Android Tests

Espresso driver provides a way for users to automate multiple Android sessions on a single machine on single server instance. All it involves is starting Appium server on any available port.

> **Note**
> It is not possible to have more than one session running on the _same_ device.

Important capabilities:

- `udid` The destination device id
- `systemPort` Set a unique port number for each parallel server session
- `chromedriverPort` If you apply hybrid mode and use [appium-chromedriver](https://github.com/appium/appium-chromedriver), set a unique ChromeDriver port for each parallel session. Otherwise, you might get a port conflict with the error message `Address already in use (48)` in the ChromeDriver log.

### Troubleshooting

When running on Jenkins, watch out for the [ProcessTreeKiller](https://wiki.jenkins.io/display/JENKINS/ProcessTreeKiller) when running multiple parallel test jobs on the same machine. If you are spawning simulators in one test job, Jenkins might kill all your simulators when the first test ends - causing errors in the remaining test jobs!

Use `BUILD_ID=dontKillMe` to prevent this from happening.
