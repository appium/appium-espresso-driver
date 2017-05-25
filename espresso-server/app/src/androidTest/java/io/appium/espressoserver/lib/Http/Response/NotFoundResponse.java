package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Model.Error;

/**
 * Created by danielgraham on 5/25/17.
 */

public class NotFoundResponse extends BaseResponse {
    public NotFoundResponse () {
        this.setHttpStatus(NanoHTTPD.Response.Status.NOT_FOUND);
        this.setResponse(new Error("Resource not found"));
    }

    public NotFoundResponse (String reason) {
        this.setHttpStatus(NanoHTTPD.Response.Status.NOT_FOUND);
        this.setResponse(new Error(reason));
    }
}
