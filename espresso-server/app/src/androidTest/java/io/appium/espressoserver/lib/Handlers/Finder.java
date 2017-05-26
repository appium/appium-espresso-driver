package io.appium.espressoserver.lib.Handlers;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import org.hamcrest.Matchers;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.Exceptions.ServerErrorException;
import io.appium.espressoserver.lib.Http.AppiumResponse;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Strategy;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;

public class Finder implements RequestHandler {

    public AppiumResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        AppiumResponse response = new AppiumResponse();
        Map<String, List<String>> parameters = session.getParameters();

        // TODO: Need to have different types of selector strategies

        final Strategy strategy;
        try {
            strategy = Strategy.fromString((String) parameters.get("strategy").get(0));
        } catch (final InvalidStrategyException e) {
            response.setResponse(new Object());
            return response;
        }

        // Get the description
        String selector = parameters.get("selector").get(0);
        //String contextId = parameters.getString("context").get(0);

        try {
            // Test the selector
            ViewInteraction matcher = findBy(strategy, selector);
            matcher.check(matches(isDisplayed()));

            // If we have a match, return success
            Element element = new Element(matcher);
            response.setResponse(element);
            return response;
        } catch (NoMatchingViewException e) {
            response.setResponse(new Object()); // TODO: Make an EmptyJSON model
            return response;
        } catch (InvalidStrategyException e) {
            response.setResponse(new Object()); // TODO: Make an EmptyJSON model
            return response;
        } catch (ServerErrorException e) {
            response.setResponse(new Object()); // TODO: Make an EmptyJSON model
            return response;
        }
    }

    ///Find By different strategies
    private ViewInteraction findBy(Strategy strategy, String selector) throws ServerErrorException, InvalidStrategyException {
        ViewInteraction matcher = null;

        try {
            switch (strategy) {
                case ID: // with ID

                    // find id from target context
                    int id = InstrumentationRegistry.getTargetContext().getResources().getIdentifier(selector, "Id",
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
                    if(matcher == null) {
                        // if text not find, check content description
                        matcher = onView(withContentDescription(selector));
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            throw new ServerErrorException();
        }

        if(matcher == null) {
            throw new InvalidStrategyException("Strategy is not implemented: " + strategy.getStrategyName());
        }

        return matcher;
    }
}