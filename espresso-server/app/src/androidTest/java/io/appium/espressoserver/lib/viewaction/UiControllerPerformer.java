package io.appium.espressoserver.lib.viewaction;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import org.hamcrest.Matcher;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static io.appium.espressoserver.lib.viewmatcher.WithView.withView;


public class UiControllerPerformer<T> implements ViewAction {

    private UiControllerRunnable<T> runnable;
    private AppiumException appiumException;
    private T runResult;

    public UiControllerPerformer(UiControllerRunnable<T> runnable) {
        this.runnable = runnable;
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
        try {
            runResult = this.runnable.run(uiController);
        } catch (AppiumException e) {
            appiumException = e;
        }
    }

    public AppiumException getAppiumException() {
        return appiumException;
    }


    public T run () throws AppiumException {

        // Get the root view because it doesn't matter what we perform this interaction on
        View rootView = (new ViewGetter()).getRootView();
        ViewInteraction viewInteraction = onView(withView(rootView));
        AndroidLogger.logger.info("Performing W3C actions sequence");

        try {
            viewInteraction.perform(this, closeSoftKeyboard());
        } catch (NoMatchingViewException nme) {
            // Ignore this. The viewMatcher is a hack to begin with
        }

        if (this.appiumException != null) {
            throw this.appiumException;
        }
        return runResult;
    }
}
