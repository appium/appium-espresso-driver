/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.http;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.helpers.Logger;
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
        Logger.info(String.format("\nRunning Appium Espresso Server at port %d \n", DEFAULT_PORT));
        router = new Router();
    }

    @Override
    public Response serve(String uri, Method method, Map<String,
            String> headers, Map<String, String> params, Map<String, String> files) {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        BaseResponse response;
        try {
            response = router.route(uri, method, params, files);
        } catch (RuntimeException e) {
            response = new ErrorResponse(e, Response.Status.INTERNAL_ERROR,
                    "Internal error has occurred");
        }

        gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
        return newFixedLengthResponse(response.getHttpStatus(),
                MediaType.APPLICATION_JSON, gsonBuilder.create().toJson(response));
    }
}