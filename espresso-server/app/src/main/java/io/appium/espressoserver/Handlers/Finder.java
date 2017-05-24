package io.appium.espressoserver.Handlers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.Http.AppiumResponse;
import io.appium.espressoserver.Model.Element;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Finder implements RequestHandler {

    public AppiumResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        AppiumResponse response = new AppiumResponse();
        Map<String, List<String>> parameters = session.getParameters();

        // TODO: Need to have different types of selector strategies
        // Get the description
        String selector = parameters.get("selector").get(0);

        try {
            // Test the selector
            ViewInteraction matcher = onView(withContentDescription(selector));
            matcher.check(matches(isDisplayed()));

            // If we have a match, return success
            Element element = new Element(matcher);
            response.setResponse(element);
            return response;
        } catch (NoMatchingViewException e) {
            response.setResponse(new Object()); // TODO: Make an EmptyJSON model
            return response;
        }
    }
}