package io.appium.espressoserver.lib.Handlers.Exceptions;

public class BadParametersException extends AppiumException {
    public BadParametersException(String reason) {
        super(reason);
    }
}
