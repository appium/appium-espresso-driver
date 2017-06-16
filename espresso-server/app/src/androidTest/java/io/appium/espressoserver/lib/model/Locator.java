package io.appium.espressoserver.lib.model;


import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class Locator extends AppiumParams {
    private Strategy using = null;
    private String value = null;

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Nullable
    public Strategy getUsing() {
        return using;
    }

    public void setUsing(Strategy using) {
        this.using = using;
    }
}
