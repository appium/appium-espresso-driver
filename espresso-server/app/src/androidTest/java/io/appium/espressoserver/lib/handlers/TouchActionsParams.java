package io.appium.espressoserver.lib.handlers;

import java.util.List;

import io.appium.espressoserver.lib.model.AppiumParams;
import io.appium.espressoserver.lib.model.TouchAction;

public class TouchActionsParams extends AppiumParams {

    private List<TouchAction> actions;

    public List<TouchAction> getActions() {
        return actions;
    }

    public void setActions(List<TouchAction> actions) {
        this.actions = actions;
    }
}
