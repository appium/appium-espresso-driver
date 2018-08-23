package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.UiController;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;


public class PerformAction implements RequestHandler<Actions, Void>  {

    @Override
    public Void handle(final Actions actions) throws AppiumException {

        UiControllerRunnable<Void> runnable = new UiControllerRunnable<Void>() {
            @Override
            public Void run(UiController uiController) throws AppiumException {
                actions.setAdapter(new EspressoW3CActionAdapter(uiController));
                actions.perform(actions.getSessionId());
                return null;
            }
        };

        new UiControllerPerformer<>(runnable).run();
        return null;
    }
}
