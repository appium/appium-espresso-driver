package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Http.Response.NotFoundResponse;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Finder implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams)  {

        // If the SessionID is invalid, return InvalidSessionResponse
        // TODO: Fix SessionID handling redundancies
        if (!uriParams.get("sessionId").equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse(uriParams.get("sessionId"));
        }

        // NanoHTTP requires call to parse body before we can get the parameters
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (NanoHTTPD.ResponseException e) {
            return new BadRequestResponse("Could not parse parameters");
        } catch (IOException e) {
            return new InternalErrorResponse("Internal server error has occurred");
        }

        AppiumResponse response = new AppiumResponse();
        Map<String, List<String>> parameters = session.getParameters();

        // TODO: Need to have different types of selector strategies
        // Get the description
        String strategy = parameters.get("using").get(0);
        String selector = parameters.get("value").get(0);

        try {
            // Test the selector
            ViewInteraction matcher;
            switch (strategy) {
                // TODO: Change this to resource-name
                case "id":
                    matcher = onView(withContentDescription(selector));
                    break;
                default:
                    return new BadRequestResponse("Invalid selector strategy: " + strategy);
            }
            matcher.check(matches(isDisplayed()));

            // If we have a match, return success
            Element element = new Element(matcher);
            response.setValue(element);
            response.setSessionId(uriParams.get("sessionId"));
            return response;
        } catch (NoMatchingViewException e) {
            return new NotFoundResponse("Could not find element: " + strategy + " " + selector);
        }
    }
}