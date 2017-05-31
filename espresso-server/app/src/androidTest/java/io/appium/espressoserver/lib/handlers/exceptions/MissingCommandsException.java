package io.appium.espressoserver.lib.handlers.exceptions;

public class MissingCommandsException extends AppiumException {
    public MissingCommandsException(String reason) {
        super(reason);
    }
}
