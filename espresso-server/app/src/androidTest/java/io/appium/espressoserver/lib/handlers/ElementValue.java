package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
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
        View view = Element.getViewById(elementId);

        try {
            if (view instanceof ProgressBar) {
                ((ProgressBar) view).setProgress(Integer.parseInt(params.getValue()));
                return null;
            }
            if (view instanceof NumberPicker) {
                ((NumberPicker) view).setValue(Integer.parseInt(params.getValue()));
                return null;
            }
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format("Cannot convert '%s' to an integer",
                    params.getValue()));
        }

        ViewInteraction viewInteraction = Element.getViewInteractionById(elementId);
        if (isReplacing) {
            viewInteraction.perform(replaceText(params.getValue()));
        } else {
            viewInteraction.perform(typeText(params.getValue()));
        }
        return null;
    }
}