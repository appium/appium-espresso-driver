package io.appium.espressoserver.lib.handlers;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.SessionNotCreatedException;
import io.appium.espressoserver.lib.model.Session;

import io.appium.espressoserver.lib.model.SessionParams;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class CreateSession implements RequestHandler<SessionParams, Session> {

    @Override
    public Session handle(SessionParams params) throws AppiumException {
        Session appiumSession = Session.createGlobalSession(params.getDesiredCapabilities());
        String activityName = params.getDesiredCapabilities().getAppActivity();
        try {
            if (activityName != null) { // TODO: Remove this, using it now for testing purposes
                startActivity(activityName);
            }
        } catch (RuntimeException e) {
            throw new SessionNotCreatedException(e.getMessage());
        }
        return appiumSession;
    }

    private void startActivity(String appActivity) {
        System.out.println("Starting activity '" + appActivity + "'");
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        ActivityMonitor mSessionMonitor = mInstrumentation.addMonitor(appActivity, null, false);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(mInstrumentation.getTargetContext(), appActivity);
        mInstrumentation.startActivitySync(intent);

        Activity mCurrentActivity = mInstrumentation.waitForMonitor(mSessionMonitor);
        System.out.println("Activity '" + mCurrentActivity.getLocalClassName() + "' started");
    }
}
