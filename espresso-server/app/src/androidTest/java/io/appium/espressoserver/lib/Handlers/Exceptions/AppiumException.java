package io.appium.espressoserver.lib.Handlers.Exceptions;

import io.appium.espressoserver.lib.Model.AppiumStatus;

/**
 * Created by danielgraham on 5/29/17.
 */

public abstract class AppiumException extends Exception {

    public AppiumException() {
        super();
    }

    public AppiumException(String reason) {
        super(reason);
    }
}
