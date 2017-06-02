package io.appium.espressoserver.lib.handlers.exceptions;

public class NotYetImplementedException extends AppiumException {
    public NotYetImplementedException() {
        super("The operation requested is not yet implemented by Espresso driver");
    }
}
