package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InternalErrorResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.action.ViewActions.typeText;


public class SendKeys implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        // If the SessionID is invalid, return InvalidSessionResponse
        // TODO: Fix SessionID handling redundancies
        if (!uriParams.get("sessionId").equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse(uriParams.get("sessionId"));
        }

        String id = uriParams.get("elementId");
        ViewInteraction viewInteraction = Element.getCache().get(id);

        // NanoHTTP requires call to parse body before we can get the parameters
        // TODO: Move parameter parsing into Router.java
        Map<String, List<String>> parameters;
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);

            Gson gson = new Gson();
            parameters = gson.fromJson(files.get("postData"), Map.class);
        } catch (NanoHTTPD.ResponseException e) {
            return new BadRequestResponse("Could not parse parameters");
        } catch (IOException e) {
            return new InternalErrorResponse("Internal server error has occurred");
        }

        AppiumResponse response = new AppiumResponse();
        String textValue = parameters.get("value").get(0);

        if (viewInteraction != null) {
            try {
                response.setAppiumId(UUID.randomUUID().toString());
                response.setSessionId(uriParams.get("sessionId")); // TODO: Automate this, too redundant
                viewInteraction.perform(typeText(textValue));
            } catch (PerformException e) {
                return new BadRequestResponse("Could not apply sendKeys to element " + id + ": " + e.getMessage());
            }
        } else {
            return new BadRequestResponse("Could not find element with ID: " + id);
        }

        response.setAppiumStatus(AppiumStatus.SUCCESS);

        return response;
    }
}
