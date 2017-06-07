package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Get the Root View of the Android App
 * Hack solution that makes use of Espresso ViewActions
 */
public class RootViewFinder {
    private final View[] views = {null};

    /**
     * To get the root view we implement a custom ViewAction that simply takes the View
     * and then saves it to an array in it's parent class.
     */
    private class GetViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            return isRoot();
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

    /**
     * This function calls the above view action which saves the view to 'views' array
     * and then returns it
     * @return The root
     */
    public View getRootView() {
        onView(isRoot()).perform(new GetViewAction());
        return views[0];
    }
}
