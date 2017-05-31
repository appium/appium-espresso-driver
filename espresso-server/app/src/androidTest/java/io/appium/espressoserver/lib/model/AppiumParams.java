package io.appium.espressoserver.lib.model;


@SuppressWarnings("unused")
public class AppiumParams {

    private String sessionId;
    private String elementId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
}
