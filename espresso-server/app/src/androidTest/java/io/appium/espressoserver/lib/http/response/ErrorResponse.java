package io.appium.espressoserver.lib.http.response;


import java.util.List;

import fi.iki.elonen.NanoHTTPD.Response.Status;

@SuppressWarnings("unused")
public class ErrorResponse extends BaseResponse {
    private final String message;
    private String reason;

    public ErrorResponse(Status status, String message) {
        httpStatus = status;
        this.message = message;
    }
    public ErrorResponse(Status status, String message, String reason) {
        httpStatus = status;
        this.message = message;
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return reason;
    }
}
