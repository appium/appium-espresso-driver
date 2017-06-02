package io.appium.espressoserver.lib.handlers;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Session;

public class DeleteSession implements RequestHandler<AppiumParams, Void> {

    @Override
    @Nullable
    public Void handle(AppiumParams params) {
        Session.deleteGlobalSession();
        return null;
    }
}
