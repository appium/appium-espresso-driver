package io.appium.espressoserver.lib.handlers.exceptions;

public class StaleElementException extends AppiumException {
    public StaleElementException(String elementId) {
        super(String.format("Element %s no longer exists", elementId));
    }
}
