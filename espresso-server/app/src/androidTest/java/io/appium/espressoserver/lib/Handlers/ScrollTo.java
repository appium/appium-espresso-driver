package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.Handlers.Exceptions.AppiumException;
import io.appium.espressoserver.lib.Handlers.Exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.Model.ScrollToParams;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ScrollTo implements RequestHandler<ScrollToParams, Object> {

    @Override
    public Object handle(ScrollToParams params) throws AppiumException {

        try {
            ViewInteraction viewInteraction = onView(withText(params.getText()));
            viewInteraction.perform(scrollTo());
        } catch (NoMatchingViewException e) {
            throw new NoSuchElementException("Could not find element with text " + params.getText());
        }

        return null;

    }


}
