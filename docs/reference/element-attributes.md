---
title: Element Attributes
---

The Espresso driver supports various native and custom element attributes. Available attributes
depend on the currently active subdriver (`espresso` or `compose`), which can be set using the
[`driver`](./settings.md#driver) setting.

## Espresso Subdriver

### `class`

> Example: `android.view.View`

Full name of the element's class. Inner classes are preferred. Could be `null`.

### `package`

> Example: `com.mycompany`

Name of the package the element belongs to, retrieved by calling [`getPackageName()`](https://developer.android.com/reference/android/content/Context#getPackageName()).

### `resource-id`

> Example: `com.mycompany:id/resId`

Resource identifier of the element, retrieved as a combination of [`getResourcePackageName`](https://developer.android.com/reference/android/content/res/Resources#getResourcePackageName(int)),
[`getResourceTypeName`](https://developer.android.com/reference/android/content/res/Resources#getResourceTypeName(int)),
and [`getResourceEntryName`](https://developer.android.com/reference/android/content/res/Resources#getResourceEntryName(int)).
Could be `null`.

### `view-tag`

> Example: `my tag`

Tag value of the element, retrieved by calling [`getTag()`](https://developer.android.com/reference/android/view/View#getTag()).

### `content-desc`

> Example: `foo`

Content description of the element, retrieved by calling [`getContentDescription()`](https://developer.android.com/reference/android/view/View#getContentDescription()).

### `text`

> Example: `my text`

Text or value of the element, depending on the element's view type:

* `TextView`: set to the value of [`getHint()`](https://developer.android.com/reference/android/widget/TextView#getHint())
  if populated, otherwise [`getText()`](https://developer.android.com/reference/android/widget/TextView#getText())
* `NumberPicker`: set to the value of [`getValue()`](https://developer.android.com/reference/android/widget/TextView#getText())
* `ProgressBar`: set to the value of [`getProgress()`](https://developer.android.com/reference/android/widget/ProgressBar#getProgress())
* All other view types: set to `null`

### `bounds`

> Example: `[0,0][100,100]`

Bounds of the element's frame as a combination of its top-left and bottom-right corner points,
formatted as `[<top-left-X>,<top-left-Y>][<bottom-right-X>,<bottom-right-Y>]`. Retrieved as a combination of [`getLocationOnScreen()`](https://developer.android.com/reference/android/view/View#getLocationOnScreen(int[])), [`getWidth()`](https://developer.android.com/reference/android/view/View#getWidth()), and
[`getHeight()`](https://developer.android.com/reference/android/view/View#getHeight()).

### `index`

> Example: `1`

Index of the element under its parent `ViewGroup`. Set to `0` if the parent view is not a `ViewGroup`.

### `checkable`

> Example: `true`

Whether the element is checkable, retrieved by determining if the element implements the
[`Checkable`](https://developer.android.com/reference/android/widget/Checkable) interface.

### `checked`

> Example: `true`

Whether the element is checked, retrieved by determining if it satisfies the [`isChecked`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher. Always `false` if the element is not [checkable](#checkable).

### `clickable`

> Example: `true`

Whether the element is clickable, retrieved by determining if it satisfies the [`isClickable`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `enabled`

> Example: `true`

Whether the element is enabled, retrieved by determining if it satisfies the [`isEnabled`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `focusable`

> Example: `true`

Whether the element is focusable, retrieved by determining if it satisfies the [`isFocusable`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `focused`

> Example: `true`

Whether the element is accessibility focused, retrieved by calling [`isAccessibilityFocused()`](https://developer.android.com/reference/android/view/View#isAccessibilityFocused()).
Always `false` if the element is not [focusable](#focusable).

### `long-clickable`

> Example: `true`

Whether the element reacts to long clicks, retrieved by calling [`isLongClickable()`](https://developer.android.com/reference/android/view/View#isLongClickable()).

### `password`

> Example: `true`

Whether the element is a password input field, retrieved by checking the element's content type
against [`TYPE_TEXT_VARIATION_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_TEXT_VARIATION_PASSWORD),
[`TYPE_TEXT_VARIATION_WEB_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_TEXT_VARIATION_WEB_PASSWORD)
and [`TYPE_NUMBER_VARIATION_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_NUMBER_VARIATION_PASSWORD).

### `scrollable`

> Example: `true`

Whether the element is scrollable, retrieved by calling [`isScrollContainer()`](https://developer.android.com/reference/android/view/View#isScrollContainer()).

### `selected`

> Example: `true`

Whether the element is selected, retrieved by determining if it satisfies the [`isSelected`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `hint`

> Example: `true`

Whether the element's `text` value is a hint. The value depends on the element's view type:

* `TextView`: set to `true` only if [`getText()`](https://developer.android.com/reference/android/widget/TextView#getText()) is empty while [`getHint()`](https://developer.android.com/reference/android/widget/TextView#getHint()) is non-empty, otherwise `false`
* `NumberPicker`, `ProgressBar`: set to `false`
* All other view types: set to `null`

### `no-multiline-buttons`

> Example: `true`

Whether the element's view hierarchy does not contain multiline buttons. This attribute is not
included in the default page source, but it can be retrieved using the standard W3C WebDriver
[Get Element Attribute](https://www.w3.org/TR/webdriver2/#get-element-attribute) endpoint.
Retrieved by calling [`noMultilineButtons()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `no-overlaps`

> Example: `true`

Whether element's descendant objects assignable to TextView or ImageView do not overlap each other.
This attribute is not included in the default page source, but it can be retrieved using the
standard W3C WebDriver [Get Element Attribute](https://www.w3.org/TR/webdriver2/#get-element-attribute)
endpoint. Retrieved by calling [`noOverlaps()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `no-ellipsized-text`

> Example: `true`

Whether the element's view hierarchy does not contain ellipsized or cut off text views.
This attribute is not included in the default page source, but it can be retrieved using the
standard W3C WebDriver [Get Element Attribute](https://www.w3.org/TR/webdriver2/#get-element-attribute)
endpoint. Retrieved by calling [`noEllipsizedText()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `visible`

> Example: `true`

Whether the element is selected, retrieved by determining if it satisfies the [`isDisplayed`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `adapter-type`

> Example: `ListAdapter`

Type of adapters contained by this `AdapterView`. Set to `null` for all element view types other
than `AdapterView`. Cannot be retrieved using the standard W3C WebDriver [Get Element Attribute](https://www.w3.org/TR/webdriver2/#get-element-attribute)
endpoint.

### `adapters`

> Example: `android.widget.CursorAdapter@45a7bc,android.widget.CursorAdapter@58db2a`

Comma-separated string of adapter objects contained by this `AdapterView`. Set to `null` for all
element view types other than `AdapterView`. Cannot be retrieved using the standard W3C WebDriver
[Get Element Attribute](https://www.w3.org/TR/webdriver2/#get-element-attribute) endpoint.

## Compose Subdriver