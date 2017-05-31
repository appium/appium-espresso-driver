package io.appium.espressoserver.lib.http;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.http.response.ErrorResponse;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.gsonadapters.AppiumStatusAdapter;

public class Server extends NanoHTTPD {

    private Router router;

    public Server() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        router = new Router();
    }

    private String[] getStackTrace(Exception e) {
        ArrayList<String> stackTrace = new ArrayList<>();
        for (StackTraceElement ste : e.getStackTrace()) {
            stackTrace.add(ste.toString());
        }
        return (String[])stackTrace.toArray();
    }

    @Override
    public Response serve(IHTTPSession session) {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        BaseResponse response;
        try {
            response = router.route(session);
        } catch (Exception e) {
            String[] stackTrace = getStackTrace(e);
            response = new ErrorResponse(Response.Status.INTERNAL_ERROR, "Internal error has occurred", stackTrace);
        }

        gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
        // TODO: Don't hardcode application/json change it to MediaType http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/MediaType.html
        return newFixedLengthResponse(response.getHttpStatus(), "application/json", gsonBuilder.create().toJson(response));
    }
}