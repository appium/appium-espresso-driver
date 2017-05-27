package io.appium.espressoserver.lib.Handlers;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Exceptions.ElementNotFoundException;
import io.appium.espressoserver.lib.Exceptions.InvalidStrategyException;
import io.appium.espressoserver.lib.Exceptions.ServerErrorException;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Model.Appium;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Strategy;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;

public class Finder extends BaseHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params)  {
        AppiumResponse response = (AppiumResponse)super.handle(session, params);

        final Strategy strategy;
        try {
            String using = (String)params.get("using");
            strategy = Strategy.fromString(using);
        } catch (final InvalidStrategyException e) {
            response.setResponse(new Appium());
            return response;
        }

        // Get the description
        String selector = (String)params.get("value");

        try {
            // Test the selector
            ViewInteraction matcher = findBy(strategy, selector);
            matcher.check(matches(isDisplayed()));

            // If we have a match, return success
            Element element = new Element(matcher);
            response.setValue(element);
            return response;
        } catch (InvalidStrategyException e) {
            return new BadRequestResponse(e.getMessage());
        } catch (ServerErrorException e) {
            return new InternalErrorResponse(e.getMessage());
        } catch (ElementNotFoundException e) {
            return new BadRequestResponse("Could not find element with " + strategy.getStrategyName() + ": " + selector);
        }
    }

    ///Find By different strategies
    private ViewInteraction findBy(Strategy strategy, String selector) throws ServerErrorException, InvalidStrategyException, ElementNotFoundException {
        ViewInteraction matcher;

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
                    break;
                case ACCESSIBILITY_ID:
                    // with content description
                    matcher = onView(withContentDescription(selector));
                    break;
                default:
                    throw new InvalidStrategyException("Strategy is not implemented: " + strategy.getStrategyName());
            }
        }
        catch (Exception e) {
            throw new ServerErrorException();
        }

        if(matcher == null) {
            throw new ElementNotFoundException();
        }

        return matcher;
    }
}
