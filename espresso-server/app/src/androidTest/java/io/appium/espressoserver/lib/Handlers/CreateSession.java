package io.appium.espressoserver.lib.Handlers;

import java.util.Map;
import io.appium.espressoserver.lib.Model.Session;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;

/**
 * Created by danielgraham on 5/25/17.
 */

public class CreateSession implements RequestHandler {

    public AppiumResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        Session appiumSession = new Session();
        AppiumResponse appiumResponse = new AppiumResponse();
        appiumResponse.setResponse(appiumSession);
        return appiumResponse;
    }
}
