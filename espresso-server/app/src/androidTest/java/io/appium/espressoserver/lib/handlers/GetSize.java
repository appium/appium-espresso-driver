package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Size;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.ViewFinder;

public class GetSize implements RequestHandler<AppiumParams, Size> {

    @Override
    public Size handle(AppiumParams params) throws AppiumException {
        final ViewInteraction viewInteraction = Element.getById(params.getElementId());
        final ViewElement viewElement = new ViewElement(new ViewFinder().getView(viewInteraction));
        final Size result = new Size();
        result.setHeight(viewElement.getBounds().height());
        result.setWidth(viewElement.getBounds().width());
        return result;
    }
}
