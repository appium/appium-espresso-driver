package io.appium.espressoserver.lib.model;

public class StartActivityParams extends AppiumParams {

    private String appActivity = null;


    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }
}
