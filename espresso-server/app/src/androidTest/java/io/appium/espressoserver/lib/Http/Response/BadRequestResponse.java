package io.appium.espressoserver.lib.Http.Response;

import io.appium.espressoserver.lib.Model.Error;
import fi.iki.elonen.NanoHTTPD;

/**
 * Created by danielgraham on 5/25/17.
 */

public class BadRequestResponse extends BaseResponse {
    public BadRequestResponse(String reason) {
        this.setHttpStatus(NanoHTTPD.Response.Status.BAD_REQUEST);
        this.setResponse(new Error(reason));
    }
}
