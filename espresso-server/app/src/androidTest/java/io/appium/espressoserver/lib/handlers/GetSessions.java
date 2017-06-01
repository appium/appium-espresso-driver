package io.appium.espressoserver.lib.handlers;

import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.singletonList;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import static io.appium.espressoserver.lib.model.Session.getGlobalSession;

public class GetSessions implements RequestHandler<AppiumParams, Collection<String>> {

    @Override
    @Nullable
    public Collection<String> handle(AppiumParams params) throws AppiumException {
        if(getGlobalSession() == null)
            return unmodifiableList(Collections.<String>emptyList());
        return unmodifiableList(singletonList(getGlobalSession().getId()));
    }

}
