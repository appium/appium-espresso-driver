package io.appium.espressoserver.lib.handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Locator;

import static io.appium.espressoserver.lib.helpers.ViewFinder.findAllBy;

public class FindElements implements RequestHandler<Locator, List<Element>> {

    @Override
    public List<Element> handle(Locator locator) throws AppiumException {
        if (locator.getUsing() == null) {
            throw new InvalidStrategyException("Locator strategy cannot be empty");
        } else if (locator.getValue() == null) {
            throw new MissingCommandsException("No locator provided");
        }

        // Get the viewInteractions
        List<ViewInteraction> viewInteractions = findAllBy(locator.getUsing(), locator.getValue());

        // Turn it into a list of elements
        List<Element> elements = new ArrayList<>();
        for(ViewInteraction viewInteraction : viewInteractions) {
            elements.add(new Element(viewInteraction));
        }

        // If we have a match, return success
        return elements;
    }
}
