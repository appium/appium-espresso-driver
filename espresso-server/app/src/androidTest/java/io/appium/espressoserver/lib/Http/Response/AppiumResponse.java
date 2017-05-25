package io.appium.espressoserver.lib.Http.Response;

import io.appium.espressoserver.lib.Model.Appium;

public class AppiumResponse extends BaseResponse {
    public AppiumResponse() {
        response = new Appium();
    }

    public void setValue(Object value) {
        this.setResponse(value);
    }

    public void setAppiumStatus(int status) {
        ((Appium)response).setStatus(status);
    }

    public void setSessionId(String sessionId) {
        ((Appium)response).setSessionId(sessionId);
    }
}

