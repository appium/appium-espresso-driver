package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ElementValueParams;

import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;

public class ElementValue implements RequestHandler<ElementValueParams, Void> {

    private final boolean isReplacing;

    public ElementValue(boolean isReplacing) {
        this.isReplacing = isReplacing;
    }

    @Override
    public Void handle(ElementValueParams params) throws AppiumException {
        String elementId = params.getElementId();
        ViewInteraction viewInteraction = Element.getViewInteractionById(elementId);
        if (!isReplacing) {
            viewInteraction.perform(typeText(params.getValue()));
        } else {
            viewInteraction.perform(replaceText(params.getValue()));
        }
        return null;
    }
}