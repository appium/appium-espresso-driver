package io.appium.espressoserver.lib.Handlers;

import java.util.Map;

import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.Session;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class CreateSession implements RequestHandler {

    @Override
    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, Object> params) {
        Session appiumSession = new Session();
        AppiumResponse appiumResponse = new AppiumResponse();

        // TODO: The Router should be deserializing the params as an Object (in this case a DesiredCapabilities object instance)
        // and if the deserialization fails, return a BadParametersError response
        Map<String, Object> desiredCaps = (Map<String, Object>)params.get("desiredCapabilities");
        String appActivity = (String)desiredCaps.get("appActivity");

        // TODO: make sure the package is the one we are expecting, erroring out otherwise

        startActivity(appActivity);

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
