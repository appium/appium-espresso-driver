package io.appium.espressoserver.lib.Handlers.Exceptions;

/**
 * Created by danielgraham on 5/29/17.
 */

public class SessionNotCreatedException extends AppiumException {

    public SessionNotCreatedException(String reason) {
        super(reason);
    }
}
