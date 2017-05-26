package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;

public class DeleteSession implements RequestHandler {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        Session.deleteGlobalSession();
        AppiumResponse appiumResponse = new AppiumResponse();
        appiumResponse.setAppiumStatus(AppiumStatus.SUCCESS);
        return appiumResponse;
    }
}
