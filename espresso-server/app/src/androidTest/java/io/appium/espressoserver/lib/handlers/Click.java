package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;

import static android.support.test.espresso.action.ViewActions.click;

public class Click implements RequestHandler<AppiumParams, Void> {

    @Override
    @Nullable
    public Void handle(AppiumParams params) throws AppiumException {
        ViewInteraction viewInteraction = Element.getById(params.getElementId());
        try {
            viewInteraction.perform(click());
        } catch (Exception e) { // TODO: Can we narrow down these exceptions?
            throw new AppiumException("Could not click element " + params.getElementId());
        }
        return null;
    }
}
