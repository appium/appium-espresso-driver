package io.appium.espressoserver.lib.Handlers;

import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.Session;

public class BaseHandler implements RequestHandler {
    public BaseResponse handle(IHTTPSession session, Map<String, Object> params) {
        if (!params.get("sessionId").equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse((String)params.get("sessionId"));
        }

        AppiumResponse response = new AppiumResponse();
        response.setSessionId((String)params.get("sessionId"));
        response.setAppiumId(UUID.randomUUID().toString());

        return response;
    }
}
