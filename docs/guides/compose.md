---
title: Jetpack Compose Support
---

The Espresso driver supports basic interactions with [Jetpack Compose](https://developer.android.com/jetpack/compose)-based applications.

Unlike the [UiAutomator2 driver](https://github.com/appium/appium-uiautomator2-driver/), which
allows interacting with Compose elements via the accessibility layer, using the 
[`testTag`](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/package-summary#(androidx.compose.ui.Modifier).testTag(kotlin.String))
attribute or the displayed text, the Espresso driver allows accessing Jetpack Compose elements
directly.

## Enabling Compose Mode

Support for Compose apps is based on the concept of subdrivers. This works similarly to Appium's
Context API: while contexts are used to switch between native and web, subdrivers are used to
switch between standard Espresso and Jetpack Compose modes, while still operating in the native
context. Each subdriver operates its own elements cache, so it is not possible to mix Espresso and
Compose elements.

The active subdriver can be changed at any time using the [`driver`](../reference/settings.md#driver)
setting. When this setting is set to `compose`, the driver switches to Compose mode, changing the behavior of various endpoints and execute methods.

## Supported APIs

The following driver functionality has Compose mode-specific behavior:

* Various endpoints
    * W3C WebDriver: [`getPageSource`](https://appium.io/docs/en/latest/reference/api/webdriver/#getpagesource), [`findElement`](https://appium.io/docs/en/latest/reference/api/webdriver/#findelement), [`findElements`](https://appium.io/docs/en/latest/reference/api/webdriver/#findelements), [`findElementFromElement`](https://appium.io/docs/en/latest/reference/api/webdriver/#findelementfromelement), [`findElementsFromElement`](https://appium.io/docs/en/latest/reference/api/webdriver/#findelementsfromelement), [`click`](https://appium.io/docs/en/latest/reference/api/webdriver/#click), [`setValue`](https://appium.io/docs/en/latest/reference/api/webdriver/#setvalue), [`clear`](https://appium.io/docs/en/latest/reference/api/webdriver/#clear), [`getAttribute`](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute), [`getName`](https://appium.io/docs/en/latest/reference/api/webdriver/#getname), [`getText`](https://appium.io/docs/en/latest/reference/api/webdriver/#gettext), [`elementDisplayed`](https://appium.io/docs/en/latest/reference/api/webdriver/#elementdisplayed), [`elementEnabled`](https://appium.io/docs/en/latest/reference/api/webdriver/#elementenabled), [`elementSelected`](https://appium.io/docs/en/latest/reference/api/webdriver/#elementselected), [`getElementRect`](https://appium.io/docs/en/latest/reference/api/webdriver/#getelementrect), [`getElementScreenshot`](https://appium.io/docs/en/latest/reference/api/webdriver/#getelementscreenshot) (since driver version 2.14.0)
    * JSONWP: [`getLocation`](../reference/endpoints.md#getlocation), [`getSize`](../reference/endpoints.md#getsize), [`keys`](../reference/endpoints.md#keys)
* Execute methods: [`mobile: swipe`](../reference/execute-methods.md#mobile-swipe) (since driver version 2.15.0)
* Only [Compose-based locators](../reference/locator-strategies.md#compose-subdriver) are supported
* Only [Compose-specific attributes](../reference/element-attributes.md#compose-subdriver) are supported

Calling other driver element-specific APIs not listed above would most likely throw an exception.

## Troubleshooting

Refer to the [Jetpack Compose Issues](../troubleshooting/compose.md) guide for handling potential
issues.

## More Resources

For more information on Compose-based driver usage and capability setup, refer to end-to-end tests:

- <https://github.com/appium/appium-espresso-driver/blob/master/test/functional/commands/jetpack-componse-element-values-e2e-specs.ts>
- <https://github.com/appium/appium-espresso-driver/blob/master/test/functional/commands/jetpack-compose-attributes-e2e-specs.ts>
- <https://github.com/appium/appium-espresso-driver/blob/master/test/functional/commands/jetpack-compose-e2e-specs.ts>
- <https://github.com/appium/appium-espresso-driver/blob/master/test/functional/commands/jetpack-compose-e2e-specs.ts>
