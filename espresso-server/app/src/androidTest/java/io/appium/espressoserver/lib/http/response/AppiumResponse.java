package io.appium.espressoserver.lib.http.response;

import java.util.UUID;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.model.AppiumStatus;

@SuppressWarnings("unused")
public class AppiumResponse<T> extends BaseResponse {
    private T value;
    private AppiumStatus status;
    private String sessionId;
    // Unique Appium transaction ID
    private String id;

    public AppiumResponse(AppiumException e, AppiumStatus status, T value) {
        e.printStackTrace();
        init(status, value, null);
    }

    public AppiumResponse(AppiumStatus status, T value) {
        init(status, value, null);
    }

    public AppiumResponse(AppiumStatus status, T value, String sessionId) {
        init(status, value, sessionId);
    }

    private void init(AppiumStatus status, T value, String sessionId) {
        this.value = value;
        this.status = status;
        this.sessionId = sessionId;
        id = UUID.randomUUID().toString();

        switch (status) {
            case SUCCESS:
                httpStatus = Status.OK;
                break;
            case UNKNOWN_COMMAND:
                httpStatus = Status.NOT_FOUND;
                break;
            default:
                httpStatus = Status.INTERNAL_ERROR;
                break;
        }
    }

    public Object getValue() {
        return value;
    }

    public Status getHttpStatus() {
        return httpStatus;
    }

    public AppiumStatus getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getId() {
        return id;
    }
}

