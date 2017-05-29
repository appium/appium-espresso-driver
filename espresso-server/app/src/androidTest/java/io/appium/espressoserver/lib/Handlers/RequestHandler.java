package io.appium.espressoserver.lib.Handlers;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumParams;

public interface RequestHandler<T extends AppiumParams>{
    BaseResponse handle(IHTTPSession session, T params);
}
