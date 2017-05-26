package io.appium.espressoserver.lib.Http;

import com.google.gson.GsonBuilder;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.Exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.Exceptions.ServerErrorException;
import io.appium.espressoserver.lib.Http.Response.BaseResponse;
import io.appium.espressoserver.lib.Model.AppiumStatus;
import io.appium.espressoserver.lib.Model.GsonAdapters.AppiumStatusAdapter;

public class Server extends NanoHTTPD {

    Router router;

    public Server() throws IOException, ServerErrorException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        try {
            router = new Router();
        } catch (DuplicateRouteException e) {
            throw new ServerErrorException();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        BaseResponse response = router.route(session);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
        // TODO: Don't harcode application/json change it to MediaType http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/MediaType.html
        return newFixedLengthResponse(response.getHttpStatus(),  "application/json", gsonBuilder.create().toJson(response.getResponse()));
    }
}