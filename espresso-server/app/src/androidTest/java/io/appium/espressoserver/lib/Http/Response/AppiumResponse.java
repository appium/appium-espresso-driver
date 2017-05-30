package io.appium.espressoserver.lib.Http.Response;

import java.util.UUID;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.appium.espressoserver.lib.Model.AppiumStatus;

@SuppressWarnings("unused")
public class AppiumResponse {
    private transient Status httpStatus;
    private Object value;
    private AppiumStatus status;
    private String sessionId;
    private String id; // Unique Appium transaction ID

    AppiumResponse(AppiumStatus status, Object value) {
        init(status, value, null);
    }

    public AppiumResponse(AppiumStatus status, Object value, String sessionId) {
        init(status, value, sessionId);
    }

    private void init(AppiumStatus status, Object value, String sessionId) {
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
            default:
                httpStatus = Status.BAD_REQUEST;
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

