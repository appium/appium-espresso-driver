package io.appium.espressoserver.lib.Model;

/**
 * Created by danielgraham on 5/25/17.
 */

public class Error {
    private String reason;

    public Error(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
