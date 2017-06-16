package io.appium.espressoserver.lib.handlers.exceptions;

public class StaleElementException extends AppiumException {
    public StaleElementException(String elementId) {
        super(String.format("The cached element %s no longer exists in the Android View hierarchy. Try to find it using a locator.", elementId));
    }
}
