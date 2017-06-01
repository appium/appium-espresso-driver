package io.appium.espressoserver.lib.handlers;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Session;
import io.appium.espressoserver.lib.model.SessionParams;

public class GetSession implements RequestHandler<AppiumParams, SessionParams.DesiredCapabilities> {

    @Override
    @Nullable
    public SessionParams.DesiredCapabilities handle(AppiumParams params) throws AppiumException {
        return Session.getGlobalSession().getDesiredCapabilities();
    }

}
