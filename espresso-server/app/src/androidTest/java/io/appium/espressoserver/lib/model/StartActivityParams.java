package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

public class StartActivityParams extends AppiumParams {

    private String appActivity;


    @Nullable
    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }
}
