package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.Handlers.Exceptions.AppiumException;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.TextParams;

import static android.support.test.espresso.action.ViewActions.typeText;


public class SendKeys implements RequestHandler<TextParams, Object> {

    @Override
    public Object handle(TextParams params) {
        String id = params.getElementId();
        ViewInteraction viewInteraction = Element.getById(id);

        // Convert the array of text to a String
        String[] textArray = params.getValue();
        StringBuilder stringBuilder = new StringBuilder();
        for (String text: textArray) {
            stringBuilder.append(text);
        }

        String textValue = stringBuilder.toString();

        try {
            viewInteraction.perform(typeText(textValue));
        } catch (PerformException e) {
            return new AppiumException("Could not apply sendKeys to element " + id + ": " + e.getMessage());
        }

        return null;
    }
}
