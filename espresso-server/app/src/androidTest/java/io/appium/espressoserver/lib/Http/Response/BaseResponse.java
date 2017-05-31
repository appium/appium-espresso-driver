package io.appium.espressoserver.lib.Http.Response;


import fi.iki.elonen.NanoHTTPD.Response.Status;

public class BaseResponse {
    protected transient Status httpStatus;

    public Status getHttpStatus() {
        return httpStatus;
    }
}
