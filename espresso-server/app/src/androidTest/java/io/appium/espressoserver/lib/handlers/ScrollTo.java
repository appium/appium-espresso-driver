package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.model.ScrollToParams;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ScrollTo implements RequestHandler<ScrollToParams, Void> {

    @Override
    @Nullable
    public Void handle(ScrollToParams params) throws AppiumException {

        try {
            ViewInteraction viewInteraction = onView(withText(params.getText()));
            viewInteraction.perform(scrollTo());
        } catch (NoMatchingViewException e) {
            throw new NoSuchElementException("Could not find element with text " + params.getText());
        }

        return null;

    }


}
