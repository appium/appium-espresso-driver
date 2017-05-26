package io.appium.espressoserver.lib.Http.Response;

import io.appium.espressoserver.lib.Model.Appium;
import io.appium.espressoserver.lib.Model.AppiumStatus;

public class AppiumResponse extends BaseResponse {
    public AppiumResponse() {
        setResponse(new Appium());
    }

    public void setValue(Object value) {
        getResponse().setValue(value);
    }

    public void setAppiumStatus(AppiumStatus status) {
        getResponse().setStatus(status);
    }

    public void setSessionId(String sessionId) {
        getResponse().setSessionId(sessionId);
    }

    public void setAppiumId(String id) {
        getResponse().setId(id);
    }
}

