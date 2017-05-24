package io.appium.espressoserver.Handlers;

import android.support.test.espresso.ViewInteraction;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.Http.AppiumResponse;
import io.appium.espressoserver.Model.Element;

import static android.support.test.espresso.action.ViewActions.click;

public class Click implements RequestHandler {

    public AppiumResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        String id = uriParams.get("id");
        ViewInteraction viewInteraction = Element.getCache().get(Integer.parseInt(id));

        AppiumResponse response = new AppiumResponse();
        if (viewInteraction != null) {
            try {
                viewInteraction.perform(click());
                response.setResponse("success");
            } catch (Exception e) {
                return response;
            }
        }

        return response;
    }
}
