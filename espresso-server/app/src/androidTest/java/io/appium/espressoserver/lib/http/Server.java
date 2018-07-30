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

import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import fi.iki.elonen.NanoHTTPD;
import io.appium.espressoserver.lib.handlers.exceptions.DuplicateRouteException;
import io.appium.espressoserver.lib.http.response.AppiumResponse;
import io.appium.espressoserver.lib.http.response.BaseResponse;
import io.appium.espressoserver.lib.model.AppiumStatus;
import io.appium.espressoserver.lib.model.gsonadapters.AppiumStatusAdapter;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;
import static io.appium.espressoserver.lib.helpers.StringHelpers.abbreviate;

public class Server extends NanoHTTPD {

    private Router router;
    private static final int DEFAULT_PORT = 8080;

    public Server() throws IOException, DuplicateRouteException {
        super(DEFAULT_PORT);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        logger.info(String.format("\nRunning Appium Espresso Server at port %d \n", DEFAULT_PORT));
        router = new Router();
    }

    private Response buildFixedLengthResponse(BaseResponse response) {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        gsonBuilder.registerTypeAdapter(AppiumStatus.class, new AppiumStatusAdapter());
        return newFixedLengthResponse(response.getHttpStatus(),
                MediaType.APPLICATION_JSON, gsonBuilder.create().toJson(response));
    }

    @Override
    public Response serve(String uri, Method method, Map<String,
            String> headers, Map<String, String> params, Map<String, String> files) {
        BaseResponse response;
        try {
            response = router.route(uri, method, params, files);
        } catch (RuntimeException e) {
            response = new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e));
        }

        if (response instanceof AppiumResponse) {
            AppiumResponse appiumResponse = (AppiumResponse) response;
            if (appiumResponse.getStatus() == AppiumStatus.SUCCESS) {
                logger.info(String.format("Responding to server with value: %s",
                        abbreviate(String.valueOf(appiumResponse.getValue()), 300)));
            } else {
                logger.info(String.format("Responding to server with error: %s",
                        appiumResponse.getValue()));
            }
        }

        try {
            return buildFixedLengthResponse(response);
        } catch (RuntimeException e) {
            return buildFixedLengthResponse(
                    new AppiumResponse<>(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e)));
        }
    }
}