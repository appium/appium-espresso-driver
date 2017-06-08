package io.appium.espressoserver.lib.handlers;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;

import static android.support.test.espresso.Espresso.pressBack;

public class Back implements RequestHandler<AppiumParams, Void> {

    @Override
    @Nullable
    public Void handle(AppiumParams params) throws AppiumException {
        pressBack();
        return null;
    }
}
