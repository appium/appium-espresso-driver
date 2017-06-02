package io.appium.espressoserver.lib.handlers.exceptions;

public class SessionNotCreatedException extends AppiumException {

    public SessionNotCreatedException(String reason) {
        super(reason);
    }
}
