package io.appium.espressoserver.lib.Handlers;

import io.appium.espressoserver.lib.Handlers.Exceptions.AppiumException;
import io.appium.espressoserver.lib.Model.AppiumParams;

public interface RequestHandler<T extends AppiumParams>{
    Object handle(T params) throws AppiumException;
}
