package io.appium.espressoserver.lib.handlers;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Session;

public class GetSessions implements RequestHandler<AppiumParams, String[]> {

    @Override
    @Nullable
    public String[] handle(AppiumParams params) throws AppiumException {
        return new String[]{ Session.getGlobalSession().getId() };
    }

}
