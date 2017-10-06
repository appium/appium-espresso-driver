package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;

import static android.support.test.espresso.action.ViewActions.replaceText;

public class Clear implements RequestHandler<AppiumParams, Void> {

    @Override
    @Nullable
    public Void handle(AppiumParams params) throws AppiumException {
        ViewInteraction viewInteraction = Element.getById(params.getElementId());
        try {
            viewInteraction.perform(replaceText(""));
        } catch (PerformException e) {
            throw new AppiumException(String.format("Could not apply clear to element %s",
                    params.getElementId()), e);
        }
        return null;
    }
}
