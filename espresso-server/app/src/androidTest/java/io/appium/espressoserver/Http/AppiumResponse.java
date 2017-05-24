package io.appium.espressoserver.Http;

/**
 * Created by danielgraham on 5/24/17.
 */

public class AppiumResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
