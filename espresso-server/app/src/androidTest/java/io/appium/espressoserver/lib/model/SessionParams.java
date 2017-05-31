package io.appium.espressoserver.lib.model;

@SuppressWarnings("unused")
public class SessionParams extends AppiumParams {
    public DesiredCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    @SuppressWarnings("unused")
    public class DesiredCapabilities {
        private String appActivity;

        public String getAppActivity() {
            return appActivity;
        }
    }

    private DesiredCapabilities desiredCapabilities;
}
