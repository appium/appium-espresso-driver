package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;


public class ActionsPerformer implements ViewAction {

    private final Actions actions;
    private AppiumException appiumException;

    public ActionsPerformer(Actions actions) {
        this.actions = actions;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "applying W3C actions ";
    }

    @Override
    public void perform(UiController uiController, View view) {
        actions.setAdapter(new EspressoW3CActionAdapter(uiController));
        try {
            actions.performActions(actions.getSessionId());
        } catch (AppiumException e) {
            this.appiumException = e;
        }
    }

    public AppiumException getAppiumException() {
        return appiumException;
    }
}
