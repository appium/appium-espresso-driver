package io.appium.espressoserver.lib.handlers;

import java.util.List;

import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.TouchAction;

public class MultiTouchActionsParams extends AppiumParams {

    private List<List<TouchAction>> actions;

    public List<List<TouchAction>> getActions() {
        return actions;
    }

    public void setActions(final List<List<TouchAction>> actions) {
        this.actions = actions;
    }
}
