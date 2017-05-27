package io.appium.espressoserver.lib.Http.Response;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Model.Appium;

public abstract class BaseResponse {

    private NanoHTTPD.Response.Status httpStatus;
    private Appium response;


    BaseResponse() {
        super();
        httpStatus = NanoHTTPD.Response.Status.OK;
    }

    public Appium getResponse() {
        return response;
    }

    public void setResponse(Appium response) {
        this.response = response;
    }

    public NanoHTTPD.Response.Status getHttpStatus() {
        return httpStatus;
    }

    void setHttpStatus(NanoHTTPD.Response.Status httpStatus) {
        this.httpStatus = httpStatus;
    }
}
