package io.appium.espressoserver.lib.Http.Response;

import io.appium.espressoserver.lib.Model.Error;
import fi.iki.elonen.NanoHTTPD;


public class BadRequestResponse extends AppiumResponse {
    public BadRequestResponse(String reason) {
        this.setHttpStatus(NanoHTTPD.Response.Status.BAD_REQUEST);
        this.setValue(new Error(reason));
    }
}
