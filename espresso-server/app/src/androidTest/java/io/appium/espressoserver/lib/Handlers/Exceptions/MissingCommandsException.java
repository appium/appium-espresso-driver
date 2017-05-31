package io.appium.espressoserver.lib.Handlers.Exceptions;

public class MissingCommandsException extends AppiumException {
    public MissingCommandsException(String reason) {
        super(reason);
    }
}
