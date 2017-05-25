package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.Session;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;

public class CreateSession implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        Session appiumSession = new Session();
        AppiumResponse appiumResponse = new AppiumResponse();
        appiumResponse.setAppiumStatus(0);
        appiumResponse.setSessionId(appiumSession.getId());
        return appiumResponse;
    }
}
