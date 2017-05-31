package io.appium.espressoserver.lib.handlers;

import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Session;

public class DeleteSession implements RequestHandler<AppiumParams, Void> {

    @Override
    public Void handle(AppiumParams params) {
        Session.deleteGlobalSession();
        return null;
    }
}
