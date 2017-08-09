package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Get a View in an the Android App
 * Hack solution that makes use of Espresso ViewActions
 */
public class ViewFinder {
    private final View[] views = {null};

    /**
     * To get the root view we implement a custom ViewAction that simply takes the View
     * and then saves it to an array in it's parent class.
     */
    private class GetViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            // This is a hack constraint that passes any view through
            return isDescendantOfA(isRoot());
        }

        @Override
        public String getDescription() {
            return "getting view in application";
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

    public View getView(ViewInteraction viewInteraction) {
        viewInteraction.perform(new GetViewAction());
        return views[0];
    }
}
