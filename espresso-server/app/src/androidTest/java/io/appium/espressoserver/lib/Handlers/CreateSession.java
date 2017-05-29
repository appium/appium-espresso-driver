package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumParams;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Model.SessionParams;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class CreateSession implements RequestHandler<SessionParams> {

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, SessionParams params) {
        Session appiumSession = new Session();
        AppiumResponse appiumResponse = new AppiumResponse();
        startActivity(params.getDesiredCapabilities().getAppActivity());
        appiumResponse.setAppiumStatus(AppiumStatus.SUCCESS);
        appiumResponse.setSessionId(appiumSession.getId());
        return appiumResponse;
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
