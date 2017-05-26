package io.appium.espressoserver.lib.Model;

@SuppressWarnings("unused")
public class Appium {
    private AppiumStatus status;
    private String sessionId;
    private Object value;
    private String id; // UUID transaction ID

    public Appium() {
        value = new Object(); // Default is empty object {}
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public AppiumStatus getStatus() {
        return status;
    }

    public void setStatus(AppiumStatus status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
