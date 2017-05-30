package io.appium.espressoserver.lib.Handlers.Exceptions;

import io.appium.espressoserver.lib.Model.AppiumStatus;

/**
 * Created by danielgraham on 5/29/17.
 */

public class BadParametersException extends AppiumException {
    public BadParametersException(String reason) {
        super(reason);
    }
}
