package io.appium.espressoserver.lib.Handlers.Exceptions;

public class AppiumException extends Exception {

    AppiumException() {
        super();
    }

    public AppiumException(String reason) {
        super(reason);
    }
}
