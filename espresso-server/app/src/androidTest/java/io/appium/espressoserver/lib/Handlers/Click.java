package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.Handlers.Exceptions.AppiumException;
import io.appium.espressoserver.lib.Model.AppiumParams;
import io.appium.espressoserver.lib.Model.Element;

import static android.support.test.espresso.action.ViewActions.click;

public class Click implements RequestHandler<AppiumParams> {

    public Object handle(AppiumParams params) throws AppiumException {
        ViewInteraction viewInteraction = Element.getCache().get(params.getElementId());
        try {
            viewInteraction.perform(click());
        } catch (Exception e) { // TODO: Can we narrow down these exceptions?
            return new AppiumException("Could not find element " + params.getElementId()) {
            };
        }
        return null;
    }
}
