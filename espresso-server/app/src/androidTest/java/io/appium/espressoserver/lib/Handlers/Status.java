package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;

public class Status implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        AppiumResponse appiumResponse = new AppiumResponse();
        appiumResponse.setAppiumStatus(0);
        return appiumResponse;
    }
}
