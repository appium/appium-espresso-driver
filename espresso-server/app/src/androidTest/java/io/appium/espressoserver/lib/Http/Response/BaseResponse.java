package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;

public abstract class BaseResponse {

    protected NanoHTTPD.Response.Status httpStatus;
    protected Object response;

    public BaseResponse() {
        super();
        httpStatus = NanoHTTPD.Response.Status.OK;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public NanoHTTPD.Response.Status getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(NanoHTTPD.Response.Status httpStatus) {
        this.httpStatus = httpStatus;
    }
}
