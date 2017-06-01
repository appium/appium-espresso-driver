package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.TextParams;

import static android.support.test.espresso.action.ViewActions.typeText;


public class SendKeys implements RequestHandler<TextParams, Void> {

    @Override
    @Nullable
    public Void handle(TextParams params) throws AppiumException {
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
            throw new AppiumException("Could not apply sendKeys to element " + id + ": " + e.getMessage());
        }

        return null;
    }
}
