package io.appium.espressoserver.lib.Handlers.Exceptions;

import io.appium.espressoserver.lib.Model.AppiumStatus;

/**
 * Created by danielgraham on 5/29/17.
 */

public class NoSuchElementException extends AppiumException {

    public NoSuchElementException() {
        super();
    }

    public NoSuchElementException(String reason) {
        super(reason);
    }

}
