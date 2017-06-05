package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Get the Root View of the Android App
 */
public class RootViewFinder {
    private final View[] views = {null};

    /**
     * ViewAction subclass that simply saves the view that's being operated on to a variable in it's
     * parent class
     */
    private class GetViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            return ViewMatchers.isRoot();
        }

        @Override
        public String getDescription() {
            return "getting root view of application";
        }

        @Override
        public void perform(UiController uiController, View view) {
            views[0] = view;
        }
    }

    public View getRootView() {
        onView(isRoot()).perform(new GetViewAction());
        return views[0];
    }
}
