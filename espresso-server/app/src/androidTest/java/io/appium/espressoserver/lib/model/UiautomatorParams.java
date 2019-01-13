package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

public class UiautomatorParams extends AppiumParams {
    private String byMethod;
    private String value;
    private Integer index;
    private String action;

    public String getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }
    public String getBy() {
        return byMethod;
    }

    @Nullable
    public Integer getIndex() {
        return index;
    }
}
