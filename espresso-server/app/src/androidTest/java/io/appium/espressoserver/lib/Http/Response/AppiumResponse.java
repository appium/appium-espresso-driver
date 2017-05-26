package io.appium.espressoserver.lib.Http.Response;

import io.appium.espressoserver.lib.Model.Appium;
import io.appium.espressoserver.lib.Model.AppiumStatus;

public class AppiumResponse extends BaseResponse {
    public AppiumResponse() {
        setResponse(new Appium());
    }

    public void setValue(Object value) {
        ((Appium)response).setValue(value);
    }

    public void setAppiumStatus(AppiumStatus status) {
        ((Appium)response).setStatus(status);
    }

    public void setSessionId(String sessionId) {
        ((Appium)response).setSessionId(sessionId);
    }

    public void setAppiumId(String id) {
        ((Appium)response).setId(id);
    }
}

