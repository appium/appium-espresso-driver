package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.ViewAttributesEnum;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewTextGetter;

public class ElementEquals implements RequestHandler<AppiumParams, Boolean> {

    @Override
    @Nullable
    public Boolean handle(AppiumParams params) throws AppiumException {
        final String elementId = params.getElementId();
        final String otherElementId = params.getUriParameterValue("otherId");
        View viewOne = Element.getViewById(elementId);
        View viewTwo = Element.getViewById(otherElementId);
        return viewOne.equals(viewTwo);
    }
}