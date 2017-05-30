package io.appium.espressoserver.lib.Handlers;

import java.io.Serializable;

import io.appium.espressoserver.lib.Handlers.Exceptions.AppiumException;
import io.appium.espressoserver.lib.Model.AppiumParams;

public interface RequestHandler<T extends AppiumParams, R extends Object>{
    R handle(T params) throws AppiumException;
}
