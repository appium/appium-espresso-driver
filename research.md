# Research

## References

* [Google Espresso docs](https://google.github.io/android-testing-support-library/docs/espresso/)
* [Idling Resource](https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/)
* [Espresso vs. UIAutomator](https://stackoverflow.com/questions/31076228/android-testing-uiautomator-vs-espresso)
* [Espresso + UIAutomator](http://alexzh.com/tutorials/android-testing-espresso-uiautomator-together/)

## Notes

* Many of the Espresso tests will map to Appium
  * e.g.)
    ```java
      onView(withId(R.id.name_field))
        .perform(typeText("Steve"));
    ```
     Would map to something like
     ```javascript
       findElementByName('name_field').sendKeys('Steve');
     ```
* Make use of https://github.com/JakeWharton/okhttp-idling-resource or something like it to know when resources idling
* Recommend to turn off System Animations
* Run an instance of WebDriver and run espresso queries
* Do we have to choose between Espresso and UIAutomator? Maybe not.
* Espresso waits for UI Events to complete before it moves to next part of test
* How do we switch between apps and processes?
* Need separate instance of Espresso for different processes.
* We need to test if we can:
  * Run an HTTP server on an app
  * Write test suites that run Espresso tests on arbitrary apps

## Sample Code

How to run an espresso testcase from the command line:

* `adb shell am instrument -w -r -e debug false com.example.android.testing.espresso.BasicSample.test/android.support.test.runner.AndroidJUnitRunner`

## Most relevant bits of the Espresso API

* `UiController` interface has methods like `injectKeyEvent` and `injectMotionEvent` which allow building arbitrary actions
* `EspressoKey.Builder` for building key codes with meta states
* `GeneralClickAction` for taking tapper, coordinates, and precision and getting a click action
* `GeneralSwipeAction`
* `KeyEventAction` for turning EspressoKey into an action
* `MotionEvents` for sending events to a UiController. Events include up, down, move, and cancel
* `ReplaceTextAction` for setting value of an EditText
* `ScrollToAction`
* `TypeTextAction` (includes option for tapping to focus)
* Pre-baked `ViewActions`:
    * `clearText`
    * `click`
    * `closeSoftKeyboard`
    * `doubleClick`
    * `longClick`
    * `pressBack`
    * `pressImeActionButton`
    * `pressKey`
    * `pressMenuKey`
    * `replaceText`
    * `scrollTo`
    * `swipeDown`
    * `swipeLeft`
    * `swipeRight`
    * `swipeUp`
    * `typeText`
    * `typeTextIntoFocusedView`
* `ViewMatchers`
    * `hasContentDescription`
    * `hasDescendant`
    * `hasSibling` (matches based on sibling)
    * `isChecked`
    * `isClickable`
    * `isCompletelyDisplayed`
    * `isDescendantOfA`
    * `isDisplayed`
    * `isEnabled`
    * `isFocusable`
    * `isNotChecked`
    * `isRoot`
    * `isSelected`
    * `withChild`
    * `withClassName`
    * `withContentDescription`
    * `withHint`
    * `withId` (need to use ResourceName instead since string)
    * `withInputType`
    * `withParent`
    * `withResourceName`
    * `withSpinnerText`
    * `withTagKey`
    * `withTagValue`
    * `withText`

## Full(er) Espresso API

* `UiController` (`android.support.test.espresso.UiController`): an interface for base-level UI operations. Note that there is a restriction such that these injected events can only interact with the AUT, not other apps.
    * `injectKeyEvent` (could be useful for sending non-standard keys)
    * `injectMotionEvent` (for swipes, gestures, etc)
    * `injectString` (series of key events)
* `ViewAction` (`android.support.test.espresso.ViewAction`): an interface for performing interactions with view elements
    * `perform`
* `ViewAssertion` (`android.support.test.espresso.ViewAssertion`): an interface for making assertions on views
    * `check`
* `ViewFinder` (`android.support.test.espresso.ViewFinder`): an interface for finding views
    * `getView` (finds a single view within the hierarchy; throws if multiple views or no views are found)
* `Espresso` (`android.support.test.espresso.Espresso`)
    * `closeSoftKeyboard`
    * `onData` (creates a DataInteraction)
    * `onView` (creates a ViewInteraction)
    * `openActionBarOverflowOrOptionsMenu`
    * `openContextualActionModeOverflowMenu`
    * `pressBack`
* `ViewInteraction` (`android.support.test.espresso.ViewInteraction`): primary interface for providing action or assert on a view
    * `check`
    * `perform`
    * `inRoot` (scope the ViewInteraction to the root selected by a given root matcher)
* `LayoutMatchers` (`android.support.test.espresso.matcher.LayoutMatchers`)
    * `hasEllipsizedText` (matches text views which have elided text)
    * `hasMultilineText` (matches multiline text views)
* `RootMatchers` (`android.support.test.espresso.matcher.RootMatchers`)
    * `isDialog` (matches roots which are dialogs)
    * `isFocusable`
    * `isPlatformPopup`
    * `isTouchable`
    * `withDecorView`
* `ViewMatchers` (`android.support.test.espresso.matcher.ViewMatchers`)
    * `hasContentDescription`
    * `hasDescendant`
    * `hasErrorText` (matches EditText based on error string value)
    * `hasFocus`
    * `hasImeAction`
    * `hasLinks`
    * `hasSibling` (matches based on sibling)
    * `isChecked`
    * `isClickable`
    * `isCompletelyDisplayed`
    * `isDescendantOfA`
    * `isDisplayed`
    * `isDisplayingAtLeast`
    * `isEnabled`
    * `isFocusable`
    * `isJavascriptEnabled` (for webviews)
    * `isNotChecked`
    * `isRoot`
    * `isSelected`
    * `supportsInputMethods`
    * `withChild`
    * `withClassName`
    * `withContentDescription`
    * `withEffectiveVisibility`
    * `withHint`
    * `withId` (need to use ResourceName instead since string)
    * `withInputType`
    * `withParent`
    * `withResourceName`
    * `withSpinnerText`
    * `withTagKey`
    * `withTagValue`
    * `withText`
* `DrawerActions` (`android.support.test.espresso.contrib.DrawerActions`)
    * `close`
    * `open`
* `DrawerMatchers` (`android.support.test.espresso.contrib.DrawerMatchers`)
    * `isClosed`
    * `isOpen`
* `NavigationViewActions` (`android.support.test.espresso.contrib.NavigationViewActions`)
    * `navigateTo` (navigate to a menu item using a menu item resource id)
* `PickerActions` (`android.support.test.espresso.contrib.PickerActions`)
    * `setDate`
    * `setTime`
* `RecyclerViewActions` (`android.support.test.espresso.contrib.RecyclerViewAction`)
    * `actionOnHolderItem`
    * `actionOnItem`
    * `actionOnItemAtPosition`
    * `scrollTo`
    * `scrollToHolder`
    * `scrollToPosition`
* `ActiveRootLister` (`android.support.test.espresso.base.ActiveRootLister`)
    * `listActiveRoots` (could be nice for building XML?)
* `RootViewPicker` (`android.support.test.espresso.base.RootViewPicker`)
    * `get` (gets the root view of the top-most window with which the user can interact)
* `EspressoKey.Builder` for building key codes with meta states
* `GeneralClickAction` for taking tapper, coordinates, and precision and getting a click action
* `GeneralSwipeAction`
* `KeyEventAction` for turning EspressoKey into an action
* `MotionEvents` for sending events to a UiController. Events include up, down, move, and cancel
* `ReplaceTextAction` for setting value of an EditText
* `ScrollToAction`
* `TypeTextAction` (includes option for tapping to focus)


## Instrumenting an app that is not the one the tests are associated with

### App under test (AUT)
* Resign with the same certificate as the test runner will be signed
* Install

### Appium test runner

* `apktool d app-debug-androidTest.apk`
* edit `AndroidManifest.xml` to set the instrumentation `android:targetPackage` to the package for the AUT
* `apktool b app-debug-androidTest -o a.apk`
* `zipalign` the `a.apk` file (`zipalign -v -p 4 a.apk a-aligned.apk`)
* sign the aligned file (`apksigner sign --ks my-release-key.jks --out a-release.apk a-aligned.apk`)
* remove the old runner (`adb uninstall io.appium.espresso.BasicSample.test`)
* install the new runner (`adb push a-release.apk /data/local/tmp/io.appium.espresso.BasicSample.test && adb shell pm install -t -r "/data/local/tmp/io.appium.espresso.BasicSample.test"`)

Within the test the package/activity that is passed in can be launched:
```java
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

// ...

final String CLASS_NAME = "io.appium.android.apis.ApiDemos";

Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
ActivityMonitor mSessionMonitor = mInstrumentation.addMonitor(CLASS_NAME, null, false);
Intent intent = new Intent(Intent.ACTION_MAIN);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.setClassName(i.getTargetContext(), CLASS_NAME);
mInstrumentation.startActivitySync(intent);

Activity mCurrentActivity = mInstrumentation.waitForMonitor(mSessionMonitor);
assertNotNull(mCurrentActivity);

```

### Running the test

`adb shell am instrument -w -r -e debug false io.appium.espresso.BasicSample.test/android.support.test.runner.AndroidJUnitRunner`
