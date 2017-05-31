package io.appium.espressoserver.lib.Http.Response;


import fi.iki.elonen.NanoHTTPD.Response.Status;

public abstract class BaseResponse {
    protected transient Status httpStatus;

    public Status getHttpStatus() {
        return httpStatus;
    }
}
