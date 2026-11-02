---
title: Element Attributes
---

The Espresso driver supports various native and custom element attributes. Available attributes
depend on the currently active subdriver (`espresso` or `compose`), which can be set using the
[`driver`](./settings.md#driver) setting.

## Espresso Subdriver

### `adapter-type`

> Example: `ListAdapter`

Type of adapters contained by this `AdapterView`. Set to `null` for all element view types other
than `AdapterView`. Cannot be retrieved using [the standard Get Element Attribute endpoint](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute).

### `adapters`

> Example: `android.widget.CursorAdapter@45a7bc,android.widget.CursorAdapter@58db2a`

Comma-separated string of adapter objects contained by this `AdapterView`. Set to `null` for all
element view types other than `AdapterView`. Cannot be retrieved using [the standard Get Element Attribute endpoint](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute).

### `bounds`

> Example: `[0,0][100,100]`

Bounds of the element's frame as a combination of its top-left and bottom-right corner points,
formatted as `[<top-left-X>,<top-left-Y>][<bottom-right-X>,<bottom-right-Y>]`. Retrieved as a
combination of [`View.getLocationOnScreen()`](https://developer.android.com/reference/android/view/View#getLocationOnScreen(int[])),
[`View.getWidth()`](https://developer.android.com/reference/android/view/View#getWidth()), and
[`View.getHeight()`](https://developer.android.com/reference/android/view/View#getHeight()).

### `checkable`

> Example: `true`

Whether the element is checkable, retrieved by determining if the element implements the
[`Checkable`](https://developer.android.com/reference/android/widget/Checkable) interface.

### `checked`

> Example: `true`

Whether the element is checked, retrieved by determining if it satisfies the [`isChecked`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher. Always `false` if the element is not [checkable](#checkable).

### `class`

> Example: `android.view.View`

Full name of the element's class. Inner classes are preferred. Could be `null`.

### `clickable`

> Example: `true`

Whether the element is clickable, retrieved by determining if it satisfies the [`isClickable`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `content-desc`

> Example: `foo`

Content description of the element, retrieved from [`View.getContentDescription()`](https://developer.android.com/reference/android/view/View#getContentDescription()).

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

Whether the element is accessibility focused, retrieved from [`View.isAccessibilityFocused()`](https://developer.android.com/reference/android/view/View#isAccessibilityFocused()).
Always `false` if the element is not [focusable](#focusable).

### `hint`

> Example: `true`

Whether the element's `text` value is a hint. The value depends on the element's view type:

* `TextView`: set to `true` only if [`getText()`](https://developer.android.com/reference/android/widget/TextView#getText()) is empty while [`getHint()`](https://developer.android.com/reference/android/widget/TextView#getHint()) is non-empty, otherwise `false`
* `NumberPicker`, `ProgressBar`: set to `false`
* All other view types: attribute is omitted

### `index`

> Example: `1`

Index of the element under its parent `ViewGroup`. Set to `0` if the parent view is not a `ViewGroup`.

### `long-clickable`

> Example: `true`

Whether the element reacts to long clicks, retrieved from [`View.isLongClickable()`](https://developer.android.com/reference/android/view/View#isLongClickable()).

### `no-ellipsized-text`

> Example: `true`

Whether the element's view hierarchy does not contain ellipsized or cut off text views.
This attribute is not included in the default page source, but it can be retrieved using [the standard Get Element Attribute endpoint](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute).
Retrieved by calling [`noEllipsizedText()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `no-multiline-buttons`

> Example: `true`

Whether the element's view hierarchy does not contain multiline buttons. This attribute is not
included in the default page source, but it can be retrieved using [the standard Get Element Attribute endpoint](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute).
Retrieved by calling [`noMultilineButtons()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `no-overlaps`

> Example: `true`

Whether element's descendant objects assignable to TextView or ImageView do not overlap each other.
This attribute is not included in the default page source, but it can be retrieved using [the standard Get Element Attribute endpoint](https://appium.io/docs/en/latest/reference/api/webdriver/#getattribute).
Retrieved by calling [`noOverlaps()`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/assertion/LayoutAssertions.java).

### `package`

> Example: `com.mycompany`

Name of the package the element belongs to, retrieved from [`Context.getPackageName()`](https://developer.android.com/reference/android/content/Context#getPackageName()).

### `password`

> Example: `true`

Whether the element is a password input field, retrieved by checking the element's content type
against [`TYPE_TEXT_VARIATION_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_TEXT_VARIATION_PASSWORD),
[`TYPE_TEXT_VARIATION_WEB_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_TEXT_VARIATION_WEB_PASSWORD)
and [`TYPE_NUMBER_VARIATION_PASSWORD`](https://developer.android.com/reference/android/text/InputType#TYPE_NUMBER_VARIATION_PASSWORD).

### `resource-id`

> Example: `com.mycompany:id/resId`

Resource identifier of the element, retrieved as a combination of [`Resources.getResourcePackageName()`](https://developer.android.com/reference/android/content/res/Resources#getResourcePackageName(int)),
[`Resources.getResourceTypeName()`](https://developer.android.com/reference/android/content/res/Resources#getResourceTypeName(int)),
and [`Resources.getResourceEntryName()`](https://developer.android.com/reference/android/content/res/Resources#getResourceEntryName(int)).
Could be `null`.

### `scrollable`

> Example: `true`

Whether the element is scrollable, retrieved from [`View.isScrollContainer()`](https://developer.android.com/reference/android/view/View#isScrollContainer()).

### `selected`

> Example: `true`

Whether the element is selected, retrieved by determining if it satisfies the [`isSelected`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

### `text`

> Example: `my text`

Text or value of the element, depending on the element's view type:

* `TextView`: set to the value of [`getHint()`](https://developer.android.com/reference/android/widget/TextView#getHint())
  if populated, otherwise [`getText()`](https://developer.android.com/reference/android/widget/TextView#getText())
* `NumberPicker`: set to the value of [`getValue()`](https://developer.android.com/reference/android/widget/NumberPicker#getValue())
* `ProgressBar`: set to the value of [`getProgress()`](https://developer.android.com/reference/android/widget/ProgressBar#getProgress())
* All other view types: attribute is omitted

### `view-tag`

> Example: `my tag`

Tag value of the element, retrieved from [`View.getTag()`](https://developer.android.com/reference/android/view/View#getTag()).

### `viewIndex`

> Example: `1`

Index of the element in the full application source of the current view. Indices start from `1`.

### `visible`

> Example: `true`

Whether the element is selected, retrieved by determining if it satisfies the [`isDisplayed`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
matcher.

## Compose Subdriver

### `bounds`

> Example: `[0,0][100,100]`

Bounds of the element's frame as a combination of its top-left and bottom-right corner points,
formatted as `[<top-left-X>,<top-left-Y>][<bottom-right-X>,<bottom-right-Y>]`. Retrieved from [`SemanticsNode.boundsInWindow()`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsNode#boundsInWindow()).

### `checked`

> Example: `true`

Whether the element is checked, retrieved from [`SemanticsProperties.ToggleableState`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#ToggleableState()).

### `class`

> Example: `ComposeNode`

Name of the element's class. Usually retrieved from [`SemanticsProperties.Role`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Role())
if populated. Set to `ComposeNode` by default.

### `clickable`

> Example: `true`

Whether the element is clickable, retrieved by determining if the element responds to [`SemanticsActions.OnClick`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsActions#OnClick()).

### `content-desc`

> Example: `foo`

Content description of the element, retrieved from [`SemanticsProperties.ContentDescription`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#ContentDescription()).

### `enabled`

> Example: `true`

Whether the element is enabled, retrieved from the inverse of [`SemanticsProperties.Disabled`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Disabled()).

### `focused`

> Example: `true`

Whether the element is accessibility focused, retrieved from [`SemanticsProperties.Focused`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Focused()).

### `index`

> Example: `1`

Index of the element node under its parent. Set to `0` if the node has no parent.

### `password`

> Example: `true`

Whether the element is a password input field, retrieved from [`SemanticsProperties.Password`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Password()).

### `resource-id`

> Example: `com.mycompany:id/resId`

Resource identifier of the element, retrieved from [`SemanticsNode.id`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsNode#id()).

### `scrollable`

> Example: `true`

Whether the element is scrollable, retrieved by determining if the element responds to [`SemanticsActions.ScrollBy`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsActions#ScrollBy()).

### `selected`

> Example: `true`

Whether the element is selected, retrieved from [`SemanticsProperties.Selected`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Selected()).

### `text`

> Example: `my text`

Text or value of the element, retrieved from [`SemanticsProperties.Text`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Text()),
[`SemanticsProperties.EditableText`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#EditableText()),
or [`ProgressBarRangeInfo.current`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/ProgressBarRangeInfo#current()),
whichever is populated first.

### `view-tag`

> Example: `my tag`

Tag value of the element, retrieved from [`SemanticsProperties.TestTag`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#TestTag()).
