package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.UiController;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;


public class ReleaseActions implements RequestHandler<AppiumParams, Void>  {

    @Override
    public Void handle(final AppiumParams params) throws AppiumException {

        UiControllerRunnable<Void> runnable = new UiControllerRunnable<Void>() {
            @Override
            public Void run(UiController uiController) throws AppiumException {
                (new Actions()).release(params.getSessionId());
                return null;
            }
        };

        new UiControllerPerformer<>(runnable).run();
        return null;
    }
}
