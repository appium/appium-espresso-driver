package io.appium.espressoserver.lib.Handlers.Exceptions;

public abstract class AppiumException extends Exception {

    AppiumException() {
        super();
    }

    public AppiumException(String reason) {
        super(reason);
    }
}
