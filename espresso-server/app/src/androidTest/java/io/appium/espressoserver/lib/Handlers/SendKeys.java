package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.Handlers.Exceptions.BadParametersException;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.TextParams;

import static android.support.test.espresso.action.ViewActions.typeText;


public class SendKeys implements RequestHandler<TextParams> {

    public Object handle(TextParams params) {
        String id = params.getElementId();
        ViewInteraction viewInteraction = Element.getCache().get(id);

        String textValue = params.getValue();

        try {
            viewInteraction.perform(typeText(textValue));
        } catch (PerformException e) {
            return new BadParametersException("Could not apply sendKeys to element " + id + ": " + e.getMessage());
        }

        return null;
    }
}
