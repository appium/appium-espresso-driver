package io.appium.espressoserver.lib.model;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AppiumParams {
    private static final String SESSION_ID_PARAM_NAME = "sessionId";
    private static final String ELEMENT_ID_PARAM_NAME = "elementId";
    private final Map<String, String> uriParams = new HashMap<>();

    @Nullable
    public String getSessionId() {
        return getUriParameterValue(SESSION_ID_PARAM_NAME);
    }

    @Nullable
    public String getElementId() {
        return getUriParameterValue(ELEMENT_ID_PARAM_NAME);
    }

    public void setElementId(String elementId) {
        setUriParameterValue(ELEMENT_ID_PARAM_NAME, elementId);
    }

    public void initUriMapping(Map<String, String> params) {
        uriParams.clear();
        uriParams.putAll(params);
    }

    @Nullable
    public String getUriParameterValue(String name) {
        return uriParams.get(name);
    }

    private void setUriParameterValue(String name, String value) {
        uriParams.put(name, value);
    }
}
