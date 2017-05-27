package io.appium.espressoserver.lib.Handlers;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

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

    public BaseResponse handle(NanoHTTPD.IHTTPSession session, Map<String, String> uriParams) {
        Session appiumSession = new Session();
        AppiumResponse appiumResponse = new AppiumResponse();

        Map<String, String> appInfo = getDesiredPackageAndActivity(session);

        // TODO: make sure the package is the one we are expecting, erroring out otherwise

        startActivity(appInfo.get("appActivity"));

        appiumResponse.setAppiumStatus(AppiumStatus.SUCCESS);
        appiumResponse.setSessionId(appiumSession.getId());
        return appiumResponse;
    }

    private Map<String, String> getDesiredPackageAndActivity(NanoHTTPD.IHTTPSession session) {
        Map result = new HashMap();
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);

            Gson gson = new Gson();
            Map<String, Map> data = gson.fromJson(files.get("postData"), Map.class);

            Map<String, String> caps = data.get("desiredCapabilities");

            result.put("appPackage", caps.get("appPackage"));
            result.put("appActivity", caps.get("appActivity"));

            return result;
        } catch (Exception e) {
            // TODO: error handling
            return result;
        }
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
