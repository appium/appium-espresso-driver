package io.appium.espressoserver.Http;

import android.support.test.espresso.ViewInteraction;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class Server extends NanoHTTPD {
    private boolean opened = false;

    public Server() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (!opened) {
            final String CLASS_NAME = "io.appium.android.apis.ApiDemos";

            Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
            ActivityMonitor mSessionMonitor = mInstrumentation.addMonitor(CLASS_NAME, null, false);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(mInstrumentation.getTargetContext(), CLASS_NAME);
            mInstrumentation.startActivitySync(intent);

            opened = true;
        } 

        AppiumResponse response = new AppiumResponse(0, true, "Hello world!");
        Gson gson = new Gson();
        return newFixedLengthResponse(gson.toJson(response));
    }
}
