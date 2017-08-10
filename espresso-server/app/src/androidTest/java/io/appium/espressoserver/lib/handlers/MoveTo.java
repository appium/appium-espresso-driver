package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.MoveToParams;
import io.appium.espressoserver.lib.viewaction.ScrollTo;
import static android.support.test.espresso.action.ViewActions.scrollTo;

public class MoveTo implements RequestHandler<MoveToParams, Void> {

    @Override
    public Void handle(MoveToParams params) throws AppiumException {
        // Get a reference to the view and call onData. This will automatically scroll to the view.
        ViewInteraction viewInteraction = Element.getById(params.getElementId());

        try {
            //viewInteraction.perform(new ScrollTo(params.getXOffset(), params.getYOffset()));

            // Try performing espresso's scrollTo, which will only work if
            //   1. View is descendant of scrollView
            //   2. View is visible
            viewInteraction.perform(scrollTo());
        } catch (PerformException pe) {
            // If it doesn't meet the above conditions, use our built-in scrollTo
            viewInteraction.perform(new ScrollTo());
        }

        return null;
    }


}
