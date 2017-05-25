package io.appium.espressoserver.lib.Model;

/**
 * Created by danielgraham on 5/25/17.
 */

public class Appium {
    private int status;
    private String sessionId;
    private Object value;

    public Appium() {
        value = new Object(); // Default is empty object {}
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
