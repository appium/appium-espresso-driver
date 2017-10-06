package io.appium.espressoserver.lib.viewaction;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.helpers.ActivityFinder;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class OrientationChange implements ViewAction {
    private final int orientation;

    private OrientationChange(int orientation) {
        this.orientation = orientation;
    }

    public static ViewAction orientationLandscape() {
        return new OrientationChange(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static ViewAction orientationPortrait() {
        return new OrientationChange(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "change orientation to " + orientation;
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        ActivityFinder.findActivity(view).setRequestedOrientation(orientation);
    }
}
