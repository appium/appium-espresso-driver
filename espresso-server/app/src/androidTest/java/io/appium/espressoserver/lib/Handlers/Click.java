package io.appium.espressoserver.lib.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BadRequestResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.Element;

import static android.support.test.espresso.action.ViewActions.click;

public class Click implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        String id = uriParams.get("id");
        ViewInteraction viewInteraction = Element.getCache().get(id);

        AppiumResponse response = new AppiumResponse();
        if (viewInteraction != null) {
            try {
                viewInteraction.perform(click());
                response.setResponse("success");
            } catch (Exception e) {
                return response;
            }
        } else {
            return new BadRequestResponse("Could not find element with ID: " + id);
        }

        return response;
    }
}
