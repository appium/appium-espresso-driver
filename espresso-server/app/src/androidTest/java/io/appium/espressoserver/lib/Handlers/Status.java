package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;

public class Status implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params) {
        AppiumResponse appiumResponse = new AppiumResponse();
        appiumResponse.setAppiumStatus(AppiumStatus.SUCCESS);
        return appiumResponse;
    }
}
