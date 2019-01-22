package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class StartActivityParams extends AppiumParams {
    private String appActivity;

    @Nullable
    public String getAppActivity() {
        return appActivity;
    }
}
