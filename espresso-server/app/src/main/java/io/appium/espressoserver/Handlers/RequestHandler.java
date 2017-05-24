package io.appium.espressoserver.Handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.Http.AppiumResponse;

public interface RequestHandler {
    public AppiumResponse handle(IHTTPSession session, Map<String, String> uriParams);
}
