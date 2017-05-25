package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Model.Error;

/**
 * Created by danielgraham on 5/25/17.
 */

public class InternalErrorResponse extends BaseResponse {
    public InternalErrorResponse(String reason) {
        this.setHttpStatus(NanoHTTPD.Response.Status.INTERNAL_ERROR);
        this.setResponse(new Error(reason));
    }
}
