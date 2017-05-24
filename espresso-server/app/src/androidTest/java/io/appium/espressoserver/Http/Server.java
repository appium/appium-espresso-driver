package io.appium.espressoserver.Http;

import android.support.test.espresso.ViewInteraction;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Server extends NanoHTTPD {

    public Server() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        AppiumResponse response = new AppiumResponse();
        Gson gson = new Gson();
        response.setSuccess(true);
        response.setMessage("Hello World!");
        return newFixedLengthResponse(gson.toJson(response));
    }
}