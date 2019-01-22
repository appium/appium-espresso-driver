package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class StartActivityParams extends AppiumParams {
    private String appActivity;
    private String appPackage;

    @Nullable
    public String getAppActivity() {
        return appActivity;
    }

    @Nullable
    public String getAppPackage() {
        return appPackage;
    }
}
