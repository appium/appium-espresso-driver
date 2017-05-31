package io.appium.espressoserver.lib.http.response;


import fi.iki.elonen.NanoHTTPD.Response.Status;

@SuppressWarnings("unused")
public class ErrorResponse extends BaseResponse {
    private final String message;
    private String[] stackTrace;

    public ErrorResponse(Status status, String message) {
        httpStatus = status;
        this.message = message;
    }
    public ErrorResponse(Status status, String message, String[] stackTrace) {
        httpStatus = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }
}
