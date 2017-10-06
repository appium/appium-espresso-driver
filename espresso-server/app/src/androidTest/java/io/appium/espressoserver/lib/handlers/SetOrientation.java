package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Arrays;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.OrientationParams;
import io.appium.espressoserver.lib.viewaction.OrientationChange;

public class SetOrientation implements RequestHandler<OrientationParams, Void> {

    @Override
    @Nullable
    public Void handle(OrientationParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        String orientation = params.getOrientation();
        if (orientation == null ||
                !Arrays.asList(new String[] {"LANDSCAPE", "PORTRAIT"}).
                        contains(orientation.toUpperCase())) {
            throw new AppiumException(String.format("Screen orientation value to '%s'",
                    orientation));
        }
        try {
            if (orientation.equalsIgnoreCase("LANDSCAPE")) {
                viewInteraction.perform(OrientationChange.orientationLandscape());
            } else {
                viewInteraction.perform(OrientationChange.orientationPortrait());
            }
        } catch (Exception e) {
            throw new AppiumException(String.format("Cannot change screen orientation to '%s'",
                    orientation), e);
        }
        return null;
    }
}
