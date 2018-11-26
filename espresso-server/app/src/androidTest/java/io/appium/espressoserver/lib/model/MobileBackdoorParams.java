package io.appium.espressoserver.lib.model;

import java.util.List;

@SuppressWarnings("unused")
public class MobileBackdoorParams extends AppiumParams {
    private List<MobileBackdoorMethod> opts;

    public List<MobileBackdoorMethod> getOpts() {
        return opts;
    }

}

