package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Element;
import io.appium.espressoserver.lib.Model.Session;

import static android.support.test.espresso.action.ViewActions.click;

public class Click implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {

        // If the SessionID is invalid, return InvalidSessionResponse
        // TODO: Fix SessionID handling redundancies
        if (!uriParams.get("sessionId").equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse(uriParams.get("sessionId"));
        }

        String id = uriParams.get("elementId");
        ViewInteraction viewInteraction = Element.getCache().get(id);

        AppiumResponse response = new AppiumResponse();
        if (viewInteraction != null) {
            try {
                viewInteraction.perform(click());
                response.setAppiumId(UUID.randomUUID().toString());
                response.setSessionId(uriParams.get("sessionId")); // TODO: Automate this, too redundant
                response.setAppiumStatus(AppiumStatus.SUCCESS);
            } catch (Exception e) {
                return new BadRequestResponse("Could not find element with ID: " + id);
            }
        } else {
            return new BadRequestResponse("Could not find element with ID: " + id);
        }

        return response;
    }
}
