package io.appium.espressoserver.lib.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Session;

public class GetSessions implements RequestHandler<AppiumParams, Collection<String>> {

    @Override
    @Nullable
    public Collection<String> handle(AppiumParams params) throws AppiumException {
        // Returns all of
        List<String> list = new ArrayList<>();
        if(Session.getGlobalSession() != null)
            list.add(Session.getGlobalSession().getId());
        return Collections.unmodifiableList(list);
    }

}
