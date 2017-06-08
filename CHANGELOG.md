## [1.0.0-beta.1](https://github.com/appium/appium-espresso-driver/compare/v1.0.0-beta.1) (2017-06-08)

### Changes
* Feature: Implemented NanoHTTPD Java server that runs inside of Android and follows the MJSONWP standard
* Feature: Implemented NodeJS driver that launches the Java server and proxies requests to that server
* Feature: Can locate elements with the selector strategies xpath, class, id, text, accessibility id
* Feature: Created /session, /status, /source, /screenshot, /back endpoints
* Feature: Added handling for every MJSONWP endpoint, return NotYetImplemented for unimplemented handlers