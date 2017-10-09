package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Rect;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetRect implements RequestHandler<AppiumParams, Rect> {

    @Override
    public Rect handle(AppiumParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        final ViewElement viewElement = new ViewElement(new ViewFinder().getView(viewInteraction));
        final Rect result = new Rect();
        final android.graphics.Rect elementBounds = viewElement.getBounds();
        result.setX(elementBounds.left);
        result.setY(elementBounds.top);
        result.setHeight(elementBounds.height());
        result.setWidth(elementBounds.width());
        return result;
    }
}
