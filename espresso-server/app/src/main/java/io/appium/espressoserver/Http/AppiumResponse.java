package io.appium.espressoserver.Http;

import fi.iki.elonen.NanoHTTPD.Response;

public class AppiumResponse {
    private Response.Status status;
    private Object response;

    public AppiumResponse() {
        status = Response.Status.OK;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }
}
