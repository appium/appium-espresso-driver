package io.appium.espressoserver.Handlers;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.Http.AppiumResponse;

public interface RequestHandler {
    public AppiumResponse handle(IHTTPSession session);
}
