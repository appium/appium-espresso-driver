package io.appium.espressoserver.lib.Model;

/**
 * Created by danielgraham on 5/29/17.
 */

@SuppressWarnings("serialize")
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
