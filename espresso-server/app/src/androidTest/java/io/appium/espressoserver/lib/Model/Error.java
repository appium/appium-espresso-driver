package io.appium.espressoserver.lib.Model;

@SuppressWarnings("unused")
public class Error extends Appium {
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
