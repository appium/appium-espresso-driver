---
title: DataMatcher Selectors
---

By using the driver's [Data Matcher](../reference/locator-strategies.md#data-matcher) locator
strategy, it is possible to target elements that are not visible in the viewport, without the need
to explicitly scroll their Views on screen.

## Background

Android apps have special types of Views called [AdapterViews](https://developer.android.com/reference/android/widget/AdapterView)
(e.g.: `ScrollView`, `ListView`, `GridView`). These views have child views, but the app only
renders the child views that are in the visible viewport. However, each AdapterView also has an
"adapter" object, which stores all the data for its AdapterView's children, including the views
that are not being rendered.

When using the Data Matcher strategy, element search is based on a [Hamcrest matcher](http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matchers.html)
that selects an item from an adapter. If the item is not in the view hierarchy, Espresso
automatically scrolls it into view.

## Selector Syntax

The syntax for a datamatcher selector is in JSON format:

```js
{
  "name": "<METHOD_NAME>",
  "args": [...],
}
```

* `name` is a Hamcrest matcher method name. This defaults to the `org.hamcrest.Matchers` namespace, but fully qualifed matcher method names can be used too (e.g.: `android.support.test.espresso.matcher.CursorMatchers.withRowBlob`).
* `args` are a list of args that the method takes (can be undefined if it takes no args). These can be strings, numbers, booleans or other hamcrest matcher JSON definitions.

See the [Data Matcher locator strategy reference page](../reference/locator-strategies.md#data-matcher)
for more details.

## Example

This is a ListView taken from the source XML of an Android App:

```xml
<android.widget.ListView index="0" package="io.appium.android.apis" class="android.widget.ListView" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="true" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,210][1080,1794]" resource-id="android:id/list" adapter-type="HashMap" adapters="{contentDescription=Animation, title=Animation, intent=Intent { cmp=io.appium.android.apis/.ApiDemos (has extras) }},{contentDescription=Auto Complete, title=Auto Complete, intent=Intent { cmp=io.appium.android.apis/.ApiDemos (has extras) }}, ...}">
    <android.widget.TextView index="0" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Drag and Drop" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,148][1080,274]" text="Drag and Drop" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="1" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Expandable Lists" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,277][1080,403]" text="Expandable Lists" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="2" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Focus" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,406][1080,532]" text="Focus" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="3" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Gallery" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,535][1080,661]" text="Gallery" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="4" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Game Controller Input" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,664][1080,790]" text="Game Controller Input" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="5" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Grid" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,793][1080,919]" text="Grid" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="6" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Hover Events" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,922][1080,1048]" text="Hover Events" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="7" package="io.appium.android.apis" class="android.widget.TextView" content-desc="ImageButton" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1051][1080,1177]" text="ImageButton" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="8" package="io.appium.android.apis" class="android.widget.TextView" content-desc="ImageSwitcher" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1180][1080,1306]" text="ImageSwitcher" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="9" package="io.appium.android.apis" class="android.widget.TextView" content-desc="ImageView" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1309][1080,1435]" text="ImageView" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="10" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Layout Animation" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1438][1080,1564]" text="Layout Animation" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="11" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Layouts" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1567][1080,1693]" text="Layouts" hint="false" resource-id="android:id/text1" />
    <android.widget.TextView index="12" package="io.appium.android.apis" class="android.widget.TextView" content-desc="Lists" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" visible="true" bounds="[0,1696][1080,1822]" text="Lists" hint="false" resource-id="android:id/text1" />
</android.widget.ListView>
```

This ListView displays menu items [`Drag and Drop`, `Expandable Lists`, ... to `Lists`]. The menu
also has several more items that are outside the visible viewport, and cannot be located with
standard locators, for example, `TextClock`.

The `ListView` node in the above XML has an attribute called `adapters`, containing the data that
"backs up" the ListView:

```js
{
    contentDescription = Animation, title = Animation, intent = Intent {
        cmp = io.appium.android.apis / .ApiDemos(has extras)
    }
}, {
    contentDescription = Auto Complete,
    title = Auto Complete,
    intent = Intent {
        cmp = io.appium.android.apis / .ApiDemos(has extras)
    }
}, {
    contentDescription = Buttons,
    title = Buttons,
    intent = Intent {
        cmp = io.appium.android.apis / .view.Buttons1
    }
},
...
```

These items can be targeted using a datamatcher selector. Here are some example snippets that show
how to locate and click the off-screen `TextClock` view:

=== "JavaScript"

    ```js
    driver.findElementById("list")
      .findElement("-android datamatcher", JSON.stringify({
        "name": "hasEntry",
        "args": ["title", "TextClock"]
      }))
      .click();
    ```

=== "Ruby"

    ```ruby
    @driver.find_element(:id, 'list')
      .find_element(:data_matcher, {
        name: 'hasEntry',
        args: ['title', 'TextClock']
      }.to_json)
      .click
    ```

=== "Python"

    ```python
    driver.find_element_by_id('list')
        .find_element_by_android_data_matcher({
            name='hasEntry',
            args=['title', 'TextClock']
        })
        .click()
    ```

The above selectors are equivalent to writing this matcher in Espresso:

```java
// Espresso code (not Appium code)
onData(hasEntry("title", "textClock")
  .inAdapterView(withId("android:id/list"))
  .perform(click()));
```

In this example, we select the parent `AdapterView` using an [`id`](../reference/locator-strategies.md#id)
selector, then find a child of that view by applying a Hamcrest Matcher that matches an object with
`title="TextClock"`.

Locating the parent `AdapterView` is not necessary if the Activity only has one adapter view. In
that case, it can be omitted:

=== "JavaScript"

    ```js
    driver.findElement("-android datamatcher", JSON.stringify({
        "name": "hasEntry",
        "args": ["title", "TextClock"]
      }))
      .click();
    ```

=== "Ruby"

    ```ruby
    @driver.find_element(:data_matcher, {
      name: 'hasEntry',
      args: ['title', 'TextClock']
    }.to_json).click
    ```

=== "Python"

    ```python
    driver.find_element_by_android_data_matcher({
        name='hasEntry',
        args=['title', 'TextClock']
    }).click()
    ```

## More Examples

Here are additional examples of JSON matchers with their equivalent Espresso `onData` matcher:

### StartsWith

=== "DataMatcher"

    ```json
    {
      "name": "startsWith",
      "args": "substr" // if it's a single arg, we don't need args to be an array
    }
    ```

=== "Espresso"

    ```java
    onData(startsWith("substr"));
    ```

### Multiple Matchers

=== "DataMatcher"

    ```json
    {
      "name": "allOf",
      "args": [
        {"name": "instanceOf", "args": "Map.class"},
        {"name": "hasEntry", "args": {
          "name": "equalTo", "args": "STR"
        }},
        {"name": "is", "args": "item: 50"}
      ]
    }
    ```

=== "Espresso"

    ```java
    onData(allOf(is(instanceOf(Map.class)), hasEntry(equalTo("STR"), is("item: 50"))));
    ```

### Cursor Matchers

=== "DataMatcher"

    ```json
    {
      "name": "anyOf",
      "args": [
        {
          "name": "is",
          "args": {"name": "instanceOf", "args": "Cursor.class"}
        }, {
          "name": "withRowString",
          "args": [
            "job_title",
            {"name": "is", "args": "Barista"}
          ],
          "class": "androidx.test.espresso.matcher.CursorMatchers"
        }
      ],
      "scope": {
        "name": "isDialog",
        "class": "androidx.test.espresso.matcher.RootMatchers"
      }
    }
    ```

=== "Espresso"

    ```java
    onData(
      anyOf(
        is(instanceOf(Cursor.class)),
        CursorMatchers.withRowString("job_title", is("Barista"))
      )
    ).inRoot(isDialog());
    ```

## More Resources

* [Explanation of Views vs. Data in Espresso](https://medium.com/androiddevelopers/adapterviews-and-espresso-f4172aa853cf)
* [Espresso lists](https://developer.android.com/training/testing/espresso/lists)
* [Unlocking New Testing Capabilities with Espresso Driver by Daniel Graham #AppiumConf2019](https://www.youtube.com/watch?v=gU9EEUV5n9U)
