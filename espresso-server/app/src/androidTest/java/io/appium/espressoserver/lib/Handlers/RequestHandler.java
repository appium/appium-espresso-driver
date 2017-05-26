package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;

public interface RequestHandler {
    BaseResponse handle(IHTTPSession session, Map<String, String> uriParams);
}
