package io.appium.espressoserver.lib.Model;


@SuppressWarnings("unused")
public class Locator extends AppiumParams {
    private Strategy using;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Strategy getUsing() {
        return using;
    }

    public void setUsing(Strategy using) {
        this.using = using;
    }
}
