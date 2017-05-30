package io.appium.espressoserver.lib.Http.Response;


import io.appium.espressoserver.lib.Model.AppiumStatus;

public class ErrorResponse extends AppiumResponse<String> {
    public ErrorResponse(AppiumStatus status) {
        super(status, status.getMessage());
    }

    public ErrorResponse(AppiumStatus status, String reason) {
        super(status, status.getMessage() + ": " + reason);
    }
}
