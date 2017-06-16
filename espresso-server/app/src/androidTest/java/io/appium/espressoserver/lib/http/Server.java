package io.appium.espressoserver.lib.http;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.http.response.ErrorResponse;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.gsonadapters.AppiumStatusAdapter;
import javax.ws.rs.core.MediaType;

public class Server extends NanoHTTPD {

    private Router router;
    private static final int DEFAULT_PORT = 8080;

    public Server() throws IOException, DuplicateRouteException {
        super(DEFAULT_PORT); // TODO: Get this from an environment variable
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println(String.format("\nRunning Appium Espresso Server at port %d \n", DEFAULT_PORT));
        router = new Router();
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        BaseResponse response;
        try {
            response = router.route(uri, method, parms, files);
        } catch (RuntimeException e) {
            response = new ErrorResponse(e, Response.Status.INTERNAL_ERROR, "Internal error has occurred");
        }

        gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
        return newFixedLengthResponse(response.getHttpStatus(), MediaType.APPLICATION_JSON, gsonBuilder.create().toJson(response));
    }
}