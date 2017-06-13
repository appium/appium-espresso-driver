package io.appium.espressoserver.lib.handlers.exceptions;

public class AppiumException extends Exception {

    public AppiumException() {
        super();
    }

    public AppiumException(String reason) {
        super(reason);
    }
}
