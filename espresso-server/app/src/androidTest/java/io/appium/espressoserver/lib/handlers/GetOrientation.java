package io.appium.espressoserver.lib.handlers;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.ViewInteraction;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.ActivityFinder;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetOrientation implements RequestHandler<AppiumParams, Integer> {

    @Override
    @Nullable
    public Integer handle(AppiumParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        final Activity activity = ActivityFinder.extractFrom(
                new ViewFinder().getView(viewInteraction));
        try {
            switch (activity.getRequestedOrientation()) {
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } catch (Exception e) {
            throw new AppiumException("Cannot get screen orientation", e);
        }
    }
}
