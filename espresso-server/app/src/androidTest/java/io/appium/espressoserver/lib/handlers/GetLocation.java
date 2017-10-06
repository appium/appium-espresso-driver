package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Location;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetLocation implements RequestHandler<AppiumParams, Location> {

    @Override
    public Location handle(AppiumParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        final ViewElement viewElement = new ViewElement(new ViewFinder().getView(viewInteraction));
        final Location result = new Location();
        result.setX(viewElement.getBounds().left);
        result.setY(viewElement.getBounds().top);
        return result;
    }
}
