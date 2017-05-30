package io.appium.espressoserver.lib.Http;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.Http.Response.AppiumResponse;
import io.appium.espressoserver.lib.Http.Response.ErrorResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.GsonAdapters.AppiumStatusAdapter;

public class Server extends NanoHTTPD {

    private Router router;

    public Server() throws IOException, DuplicateRouteException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        router = new Router();
    }

    @Override
    public Response serve(IHTTPSession session) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        try {
            AppiumResponse response = router.route(session);
            gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
            // TODO: Don't hardcode application/json change it to MediaType http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/MediaType.html
            return newFixedLengthResponse(response.getHttpStatus(), "application/json", gsonBuilder.create().toJson(response));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String reason = e.getMessage() + ":" + sw.toString();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gsonBuilder.create().toJson(new ErrorResponse(AppiumStatus.UNKNOWN_ERROR, reason)));
        }
    }
}