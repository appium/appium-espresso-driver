package io.appium.espressoserver.lib.Handlers;

import java.util.UUID;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Http.Response.InvalidSessionResponse;
import io.appium.espressoserver.lib.Model.AppiumParams;
import io.appium.espressoserver.lib.Model.Session;

public abstract class BaseHandler implements RequestHandler {
    public BaseResponse handle(IHTTPSession session, AppiumParams params) {
        final String sessionId = params.getSessionId();
        if (!sessionId.equals(Session.getGlobalSessionId())) {
            return new InvalidSessionResponse(sessionId);
        }

        AppiumResponse response = new AppiumResponse();
        response.setSessionId(sessionId);
        response.setAppiumId(UUID.randomUUID().toString());

        return response;
    }
}
