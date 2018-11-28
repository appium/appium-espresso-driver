package io.appium.espressoserver.lib.handlers.exceptions;

public class IncompatibleArgumentsException extends AppiumException {
    public IncompatibleArgumentsException(String reason) {
        super(reason);
    }
}
