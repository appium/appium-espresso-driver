package io.appium.espressoserver.lib.Model;

/**
 * Created by danielgraham on 5/29/17.
 */

public class SessionParams extends AppiumParams {
    public DesiredCapabilities getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void setDesiredCapabilities(DesiredCapabilities desiredCapabilities) {
        this.desiredCapabilities = desiredCapabilities;
    }

    public class DesiredCapabilities {
        private String appActivity;

        public String getAppActivity() {
            return appActivity;
        }

        public void setAppActivity(String appActivity) {
            this.appActivity = appActivity;
        }
    }

    private DesiredCapabilities desiredCapabilities;
}
