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
        final String orientation = params.getOrientation();
        if (orientation == null ||
                !Arrays.asList(new String[] {"LANDSCAPE", "PORTRAIT"}).
                        contains(orientation.toUpperCase())) {
            throw new AppiumException(String.format("Screen orientation value to '%s'",
                    orientation));
        }
        try {
            switch (orientation.toUpperCase()) {
                case "LANDSCAPE":
                    viewInteraction.perform(OrientationChange.orientationLandscape());
                    break;
                case "PORTRAIT":
                    viewInteraction.perform(OrientationChange.orientationPortrait());
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Unknown orientation value '%s'", orientation));
            }
        } catch (Exception e) {
            throw new AppiumException(String.format("Cannot change screen orientation to '%s'",
                    orientation), e);
        }
        return null;
    }
}
