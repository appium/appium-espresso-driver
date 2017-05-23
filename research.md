# Research

## References

* [Google Espresso docs](https://google.github.io/android-testing-support-library/docs/espresso/)
* [Idling Resource](https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/)
* [Espresso vs. UIAutomator](https://stackoverflow.com/questions/31076228/android-testing-uiautomator-vs-espresso)
* [Espresso + UIAutomator]http://alexzh.com/tutorials/android-testing-espresso-uiautomator-together/

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

* `adb shell am instrument -w -r   -e debug false com.example.android.testing.espresso.BasicSample.test/android.support.test.runner.AndroidJUnitRunner`
