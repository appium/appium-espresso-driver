package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.UiController;

import java.util.Collections;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static io.appium.espressoserver.lib.model.TouchAction.toW3CInputSources;

public class TouchAction implements RequestHandler<TouchActionsParams, Void> {

    @Override
    public Void handle(final TouchActionsParams params) throws AppiumException {
        UiControllerRunnable<Void> runnable = new UiControllerRunnable<Void>() {
            @Override
            public Void run(UiController uiController) throws AppiumException {
                List<InputSource> inputSources = toW3CInputSources(Collections.singletonList(params.getActions()));
                Actions actions = new ActionsBuilder()
                        .withAdapter(new EspressoW3CActionAdapter(uiController))
                        .withActions(inputSources)
                        .build();
                actions.perform(params.getSessionId());
                actions.release(params.getSessionId());

                return null;
            }
        };

        new UiControllerPerformer<>(runnable).run();
        return null;
    }
}
