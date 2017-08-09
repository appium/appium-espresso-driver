package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.MoveToParams;
import io.appium.espressoserver.lib.viewaction.ScrollTo;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class MoveTo implements RequestHandler<MoveToParams, Void> {

    @Override
    @Nullable
    public Void handle(MoveToParams params) throws AppiumException {

        try {
            // Get a reference to the view and call onData. This will automatically scroll to the view.
            ViewInteraction viewInteraction = Element.getById(params.getElementId());
            viewInteraction.perform(new ScrollTo(params.getXOffset(), params.getYOffset()));
        } catch (PerformException pe) {
            throw new AppiumException(pe.getCause().getMessage());
        }

        return null;

    }


}
