package io.appium.espressoserver.lib.Handlers;

import io.appium.espressoserver.lib.Model.Session;

import io.appium.espressoserver.lib.Model.SessionParams;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class CreateSession implements RequestHandler<SessionParams> {

    public Session handle(SessionParams params) {
        Session appiumSession = new Session();
        String activityName = params.getDesiredCapabilities().getAppActivity();
        if (activityName != null) { // TODO: Remove this,  using it now for testing purposes
            startActivity(activityName);
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
