## [2.13.8](https://github.com/appium/appium-espresso-driver/compare/v2.13.7...v2.13.8) (2022-12-03)


### Miscellaneous Chores

* **deps-dev:** bump webdriverio from 7.27.0 to 8.0.2 ([#838](https://github.com/appium/appium-espresso-driver/issues/838)) ([2b22c11](https://github.com/appium/appium-espresso-driver/commit/2b22c11a302b74fa931065d59d894dd42ff82f6f))

## [2.13.7](https://github.com/appium/appium-espresso-driver/compare/v2.13.6...v2.13.7) (2022-12-01)


### Miscellaneous Chores

* update releaserc ([#837](https://github.com/appium/appium-espresso-driver/issues/837)) ([655c333](https://github.com/appium/appium-espresso-driver/commit/655c33323f19085adc678160666ad83f5249aa31))

## [2.13.6](https://github.com/appium/appium-espresso-driver/compare/v2.13.5...v2.13.6) (2022-11-29)

## [2.13.5](https://github.com/appium/appium-espresso-driver/compare/v2.13.4...v2.13.5) (2022-11-06)

## [2.13.4](https://github.com/appium/appium-espresso-driver/compare/v2.13.3...v2.13.4) (2022-11-06)

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
