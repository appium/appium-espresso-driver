package io.appium.espressoserver.lib.handlers;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.model.AppiumParams;

public class NotYetImplemented implements RequestHandler<AppiumParams, Void> {
    @Override
    @Nullable
    public Void handle(AppiumParams params) throws AppiumException {
        throw new NotYetImplementedException();
    }
}
