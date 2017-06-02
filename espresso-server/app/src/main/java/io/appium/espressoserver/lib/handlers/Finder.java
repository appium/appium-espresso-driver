package io.appium.espressoserver.lib.handlers;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.Locator;
import io.appium.espressoserver.lib.model.Strategy;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;

public class Finder implements RequestHandler<Locator, Element> {

    @Override
    public Element handle(Locator locator) throws AppiumException {
        try {
            if (locator.getUsing() == null) {
                throw new InvalidStrategyException("Locator strategy cannot be empty");
            } else if (locator.getValue() == null) {
                throw new MissingCommandsException("No locator provided");
            }
            // Test the selector
            ViewInteraction matcher = findBy(locator.getUsing(), locator.getValue());
            matcher.check(matches(isDisplayed()));

            // If we have a match, return success
            return new Element(matcher);
        } catch (NoMatchingViewException e) {
            throw new NoSuchElementException("Could not find element with strategy " + locator.getUsing() + " and selector " + locator.getValue());
        }
    }

    ///Find By different strategies
    private ViewInteraction findBy(Strategy strategy, String selector) throws InvalidStrategyException {
        ViewInteraction matcher;

        switch (strategy) {
            case ID: // with ID

                // find id from target context
                Context context = InstrumentationRegistry.getTargetContext();
                int id = context.getResources().getIdentifier(selector, "Id",
                        InstrumentationRegistry.getTargetContext().getPackageName());

                matcher = onView(withId(id));
                break;
            case CLASS_NAME:
                // with class name
                // TODO: improve this finder with instanceOf
                matcher = onView(withClassName(endsWith(selector)));
                break;
            case TEXT:
                // with text
                matcher = onView(withText(selector));
                break;
            case ACCESSIBILITY_ID:
                // with content description
                matcher = onView(withContentDescription(selector));
                break;
            default:
                throw new InvalidStrategyException("Strategy is not implemented: " + strategy.getStrategyName());
        }

        return matcher;
    }
}
