package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by danielgraham on 5/25/17.
 */

public class BaseResponse {

    private NanoHTTPD.Response.Status status;
    private Object response;

    public BaseResponse() {
        super();
        status = NanoHTTPD.Response.Status.OK;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public NanoHTTPD.Response.Status getStatus() {
        return status;
    }

    public void setStatus(NanoHTTPD.Response.Status status) {
        this.status = status;
    }
}
