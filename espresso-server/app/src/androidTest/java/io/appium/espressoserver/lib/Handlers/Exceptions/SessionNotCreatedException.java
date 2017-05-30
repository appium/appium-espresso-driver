package io.appium.espressoserver.lib.Handlers.Exceptions;

public class SessionNotCreatedException extends AppiumException {

    public SessionNotCreatedException(String reason) {
        super(reason);
    }
}
