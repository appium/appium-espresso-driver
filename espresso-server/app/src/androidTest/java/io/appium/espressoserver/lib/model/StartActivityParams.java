package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class StartActivityParams extends AppiumParams {
    private String appActivity;
    private String appWaitActivity;

    @Nullable
    public String getAppActivity() {
        return appActivity;
    }

    @Nullable
    public String getAppWaitActivity() {
        return appWaitActivity;
    }
}
