package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Model.Error;


public class NotFoundResponse extends AppiumResponse {
    public NotFoundResponse () {
        this.setHttpStatus(NanoHTTPD.Response.Status.NOT_FOUND);
        this.setValue(new Error("Resource not found"));
        // TODO: For this and all other error response, set the Appium Status code
    }

    public NotFoundResponse (String reason) {
        this.setHttpStatus(NanoHTTPD.Response.Status.NOT_FOUND);
        this.setValue(new Error(reason));
    }
}
