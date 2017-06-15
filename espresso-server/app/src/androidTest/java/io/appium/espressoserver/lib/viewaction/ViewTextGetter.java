package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class ViewTextGetter {

    private final View[] views = {null};

    /**
     * Hack way of getting a view. Similar to 'RootViewFinder' class.
     */
    private class GetTextViewAction implements ViewAction {
        @Override
        public Matcher<View> getConstraints() {
            // This is a hack constraint that passes any view through
            return isDescendantOfA(isRoot());
        }

        @Override
        public String getDescription() {
            return "getting a view reference";
        }

        @Override
        public void perform(UiController uiController, View view) {
            views[0] = view;
        }
    }

    public CharSequence get(ViewInteraction viewInteraction) throws AppiumException {
        try {
            viewInteraction.perform(new GetTextViewAction());
            TextView textView = (TextView) views[0];
            return textView.getText();
        } catch (ClassCastException cce) {
            throw new AppiumException(String.format("Views of class type %s have no text property", views.getClass().getName()));
        }
    }
}
