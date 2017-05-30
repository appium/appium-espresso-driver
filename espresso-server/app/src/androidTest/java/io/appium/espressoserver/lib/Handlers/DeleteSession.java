package io.appium.espressoserver.lib.Handlers;

import io.appium.espressoserver.lib.Model.AppiumParams;
import io.appium.espressoserver.lib.Model.Session;

public class DeleteSession implements RequestHandler<AppiumParams> {

    @Override
    public Object handle(AppiumParams params) {
        Session.deleteGlobalSession();
        return null;
    }
}
