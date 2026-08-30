---
title: Locator Strategies
---

The Espresso driver supports several location strategies in the native context. Available locator
strategies depend on the currently active subdriver (`espresso` or `compose`), which can be set
using the [`driver`](./settings.md#driver) setting. The list below describes each strategy along
with usage recommendations.

## Espresso Subdriver

Most locator strategies in the Espresso context are mapped to a native Espresso view matcher. There
is no dedicated Android developer documentation page on the supported view matchers, but they can
be found in the [`ViewMatchers.java`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/ViewMatchers.java)
file in the Android source code.

### Id

|Name|Speed Ranking|Example|
|---|---|---|
|`id`|⭐⭐⭐⭐⭐|`com.mycompany:id/resourceId`|

This strategy is mapped to the native Espresso `withId` matcher, which returns elements whose
resource ID matches the specified value exactly.

If the package identifier prefix is not specified, the prefix identifier of the current app under
test is added automatically.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`View.id`](https://developer.android.com/reference/android/view/View#attr_android:id)
    attribute value.

### Accessibility Id

|Name|Speed Ranking|Example|
|---|---|---|
|`accessibility id`|⭐⭐⭐⭐⭐|`my description`|

This strategy is mapped to the native Espresso `withContentDescription` matcher, which returns
elements whose content description matches the specified value exactly.

If no elements are found, but matching elements exist outside the visible viewport, they will be
scrolled into view.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`View.contentDescription`](https://developer.android.com/reference/android/view/View#attr_android:contentDescription)
    attribute value.

### Class Name

|Name|Speed Ranking|Example|
|---|---|---|
|`class name`|⭐⭐⭐⭐⭐|`android.view.View`|

This strategy is mapped to the native Espresso `withClassName` matcher, which returns elements
whose class name matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their class name.

### Text

|Name|Speed Ranking|Example|
|---|---|---|
|`text`|⭐⭐⭐⭐⭐|`my text`|

This strategy is mapped to the native Espresso `withText` matcher, which returns `TextView`
elements whose text matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`TextView.text`](https://developer.android.com/reference/android/widget/TextView#attr_android:text)
    attribute value.

### View Tag

|Name|Speed Ranking|Example|
|---|---|---|
|`-android viewtag`|⭐⭐⭐⭐⭐|`my tag`|

This strategy is mapped to the native Espresso `withTagValue` matcher, which returns elements whose
text matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`View.tag`](https://developer.android.com/reference/android/view/View#attr_android:tag)
    attribute value.

### Tag Name

|Name|Speed Ranking|Example|
|---|---|---|
|`tag name`|⭐⭐⭐⭐⭐|`my tag`|

This strategy is an alias for the [`-android viewtag` strategy](#view-tag).

### Data Matcher

|Name|Speed Ranking|Example|
|---|---|---|
|`-android datamatcher`|⭐⭐⭐⭐|`{"name": "hasEntry", "args": ["title", "WebView3"]}`|

This strategy allows to create native selectors based on the Espresso [`onData`](https://developer.android.com/reference/androidx/test/espresso/Espresso#onData(org.hamcrest.Matcher%3C?%20extends%20java.lang.Object%3E))
method. Such selectors can find elements outside of the visible viewport and automatically scroll
them into view, but may need to be scoped to the relevant parent `AdapterView` element.

The selector syntax for this strategy is a JSON matcher. It is a map with the following properties:

|Name|<div style="width:11em">Type</div>|Description|
|--|--|--|
|`name`|`string`|Name of the matcher function. Supported matcher names can be found in the [Hamcrest Matchers documentation](https://hamcrest.org/JavaHamcrest/javadoc/3.0/org/hamcrest/Matchers.html). Non-Hamcrest matchers can also be used by changing the `class` property.|
|`args?`|`string` or `Array<string>` or `Array<Map<string, string>>`|One or more arguments passed to the matcher function. Each argument can itself be a JSON matcher (with `name`, `args` and `class` properties)|
|`class?`|`string`|Full qualified class name of the matcher specified in `name`. Set to `org.hamcrest.Matchers` by default. Must be changed if using matchers from other classes. Class names without the full package name are automatically prepended with `androidx.test.espresso.matcher`.|
|`scope?`|`Map<string, string>`|JSON matcher for limiting the parent scope of this selector. Its name should be a valid [`RootMatchers`](https://cs.android.com/androidx/android-test/+/main:espresso/core/java/androidx/test/espresso/matcher/RootMatchers.java) matcher, with its class set to `androidx.test.espresso.matcher.RootMatchers`.|

Refer to the [Espresso DataMatcher Selector guide](../guides/datamatcher-selector.md) for examples
on how to construct these locators.

!!! info "When to Use"

    Use this strategy for elements that may be located outside the visible viewport.

### View Matcher

|Name|Speed Ranking|Example|
|---|---|---|
|`-android viewmatcher`|⭐⭐⭐⭐|`{"name": "hasEntry", "args": ["title", "WebView3"]}`|

This strategy allows to create native selectors based on the Espresso [`onView`](https://developer.android.com/reference/androidx/test/espresso/Espresso#onView(org.hamcrest.Matcher%3Candroid.view.View%3E))
method. The syntax of these selectors is identical to that of the [`-android datamatcher`](#data-matcher)
strategy, but they can only find elements in the visible viewport, and do not need to be scoped to
an `AdapterView` element.

!!! info "When to Use"

    Use this strategy for elements located in the visible viewport, that cannot be identified by
    their class name, `id`, `contentDescription`, `text` or `tag`.

### XPath

|Name|Speed Ranking|Example|
|---|---|---|
|`xpath`|⭐⭐⭐|`//android.view.View[@text="Regular" and @checkable="true"]`|

This strategy relies on the XML tree generated by the Espresso driver's page source API.

Only XPath 1.0 is supported.

!!! info "When to Use"

    Use this strategy when no other strategy is applicable, typically for elements that can only be
    identified from their sibling or descendant elements.

## Compose Subdriver

Most locator strategies in the Compose context are mapped to a [native Compose view matcher](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/package-summary).

### Accessibility Id

|Name|Speed Ranking|Example|
|---|---|---|
|`accessibility id`|⭐⭐⭐⭐⭐|`my description`|

This strategy is mapped to the native Compose [`hasContentDescription`](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/package-summary#hasContentDescription(kotlin.String,kotlin.Boolean,kotlin.Boolean))
matcher, which returns elements whose content description matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`SemanticsProperties.ContentDescription`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#ContentDescription())
    attribute value.

### View Tag

|Name|Speed Ranking|Example|
|---|---|---|
|`-android viewtag`|⭐⭐⭐⭐⭐|`my tag`|

This strategy is mapped to the native Compose [`hasTestTag`](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/package-summary#hasTestTag(kotlin.String))
matcher, which returns elements whose text matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`SemanticsProperties.TestTag`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#TestTag())
    attribute value.

### Tag Name

|Name|Speed Ranking|Example|
|---|---|---|
|`tag name`|⭐⭐⭐⭐⭐|`my tag`|

This strategy is an alias for the [`-android viewtag` strategy](#view-tag_1).

### Text

|Name|Speed Ranking|Example|
|---|---|---|
|`text`|⭐⭐⭐⭐⭐|`my text`|

This strategy is mapped to the native Compose [`hasText`](https://developer.android.com/reference/kotlin/androidx/compose/ui/test/package-summary#hasText(kotlin.String,kotlin.Boolean,kotlin.Boolean))
matcher, which returns elements whose text matches the specified value exactly.

!!! info "When to Use"

    Use this strategy for elements that can be identified using their [`SemanticsProperties.Text`](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsProperties#Text())
    attribute value.

### Link Text

|Name|Speed Ranking|Example|
|---|---|---|
|`link text`|⭐⭐⭐⭐⭐|`my tag`|

This strategy is an alias for the [`text` strategy](#text_1).

### XPath

|Name|Speed Ranking|Example|
|---|---|---|
|`xpath`|⭐⭐⭐|`//ComposeNode[@text="Regular" and @selected="true"]`|

This strategy relies on the XML tree generated by the Espresso driver's page source API.

Only XPath 1.0 is supported.

!!! info "When to Use"

    Use this strategy when no other strategy is applicable, typically for elements that can only be
    identified from their sibling or descendant elements.
