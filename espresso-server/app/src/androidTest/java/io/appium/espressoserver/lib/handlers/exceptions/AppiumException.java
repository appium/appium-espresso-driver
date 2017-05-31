package io.appium.espressoserver.lib.handlers.exceptions;

public class AppiumException extends Exception {

    AppiumException() {
        super();
    }

    public AppiumException(String reason) {
        super(reason);
    }
}
