package io.appium.espressoserver.lib.http.response;


import java.util.List;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

@SuppressWarnings("unused")
public class ErrorResponse extends BaseResponse {
    private final String message;
    private String reason;

    public ErrorResponse(Status status, String message) {
        httpStatus = status;
        this.message = message;
    }
    public ErrorResponse(Exception e, Status status, String message) {
        e.printStackTrace();
        httpStatus = status;
        this.message = message;
        this.reason = e.getMessage();
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return reason;
    }
}
