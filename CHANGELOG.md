## [1.0.0-beta.3](https://github.com/appium/appium-espresso-driver/compare/v1.0.0-beta.2...v1.0.0-beta.3) (2017-06-16)

### Changes
* Fix: 	Throws exception if element is stale (#56)
* Feature: Add text attribute to source xml (#52)
* Feature: Implemented the /element/:elementId/text endpoint (#53)
* Feature: Can match list of XPaths instead of just one (#54) 
* Feature: Added handler for /elements endpoint (#55)


## [1.0.0-beta.2](https://github.com/appium/appium-espresso-driver/compare/v1.0.0-beta.1...v1.0.0-beta.2) (2017-06-09)

### Changes
* Fix: Sanitize XML inputs. Bad characters and tag names were breaking the XML (#50)

## [1.0.0-beta.1](https://github.com/appium/appium-espresso-driver/compare/7a309d3...v1.0.0-beta.1) (2017-06-08)

### Changes
* Feature: Implemented NanoHTTPD Java server that runs inside of Android and follows the MJSONWP standard
* Feature: Implemented NodeJS driver that launches the Java server and proxies requests to that server
* Feature: Can locate elements with the selector strategies xpath, class, id, text, accessibility id
* Feature: Created /session, /status, /source, /screenshot, /back endpoints
* Feature: Added handling for every MJSONWP endpoint, return NotYetImplemented for unimplemented handlers