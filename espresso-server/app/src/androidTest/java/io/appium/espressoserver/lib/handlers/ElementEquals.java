package io.appium.espressoserver.lib.handlers;

import android.view.View;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;

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