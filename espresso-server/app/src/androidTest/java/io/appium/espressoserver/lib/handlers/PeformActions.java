package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.viewaction.ActionsPerformer;
import io.appium.espressoserver.lib.viewaction.ViewGetter;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static io.appium.espressoserver.lib.viewmatcher.WithView.withView;


public class PeformActions implements RequestHandler<Actions, Void>  {

    @Override
    public Void handle(Actions actions) throws AppiumException {
        // Get the root view because it doesn't matter what we perform this interaction on
        View rootView = (new ViewGetter()).getRootView();
        ViewInteraction viewInteraction = onView(withView(rootView));
        AndroidLogger.logger.info("Performing W3C actions sequence");

        ActionsPerformer actionsPerformer = new ActionsPerformer(actions);
        try {
            viewInteraction.perform(actionsPerformer, closeSoftKeyboard());
        } catch (NoMatchingViewException nme) {
            // Ignore this. The viewMatcher is a hack to begin with
        }

        // Hacky way of getting the exception from the view interaction
        if (actionsPerformer.getAppiumException() != null) {
            throw actionsPerformer.getAppiumException();
        }

        return null;
    }
}
