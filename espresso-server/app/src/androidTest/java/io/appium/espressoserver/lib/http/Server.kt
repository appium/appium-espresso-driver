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

package io.appium.espressoserver.lib.http

import android.util.Log

import com.google.gson.GsonBuilder

import java.io.IOException
import java.net.SocketException
import java.util.LinkedHashMap

import javax.ws.rs.core.MediaType

import fi.iki.elonen.NanoHTTPD
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.StringHelpers
import io.appium.espressoserver.lib.http.response.AppiumResponse
import io.appium.espressoserver.lib.http.response.BaseResponse
import io.appium.espressoserver.lib.model.AppiumStatus
import io.appium.espressoserver.lib.model.gsonadapters.AppiumStatusAdapter

class Server private constructor() : NanoHTTPD(DEFAULT_PORT) {

    private var router: Router? = null

    @Volatile
    var isStopRequestReceived: Boolean = false
        private set

    private fun buildFixedLengthResponse(response: BaseResponse): Response {
        val gsonBuilder = GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(AppiumStatus::class.java, AppiumStatusAdapter())
        return newFixedLengthResponse(response.httpStatus,
                MediaType.APPLICATION_JSON, gsonBuilder.create().toJson(response))
    }

    override fun serve(session: IHTTPSession): Response {
        var response: BaseResponse
        val files = LinkedHashMap<String, String>()
        try {
            session.parseBody(files)
            response = router!!.route(session.uri, session.method, files)
        } catch (e: RuntimeException) {
            response = AppiumResponse(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e))
        } catch (e: IOException) {
            response = AppiumResponse(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e))
        } catch (e: ResponseException) {
            response = AppiumResponse(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e))
        }

        if (response is AppiumResponse<*>) {
            if (response.status === AppiumStatus.SUCCESS) {
                AndroidLogger.logger.info("Responding to server with value: " +
                        StringHelpers.abbreviate(response.value?.toString(), 300))
            } else {
                AndroidLogger.logger.info("Responding to server with error: " +
                        response.value)
            }
        }

        return try {
            buildFixedLengthResponse(response)
        } catch (e: RuntimeException) {
            buildFixedLengthResponse(
                    AppiumResponse(AppiumStatus.UNKNOWN_ERROR, Log.getStackTraceString(e)))
        }

    }

    @Throws(IOException::class)
    override fun start() {
        if (super.isAlive()) {
            //kill the server if its already running
            try {
                super.stop()
            } catch (e: Exception) {
                //ignore the exception
            }
        }
        try {
            super.start(SOCKET_READ_TIMEOUT, false)
        } catch (e: SocketException) {
            throw IllegalStateException("The application under test must require android.permission.INTERNET " +
                    "permission in its manifest", e)
        }

        AndroidLogger.logger.info("\nRunning Appium Espresso Server at port $DEFAULT_PORT\n")
        router = Router()
    }

    override fun stop() {
        super.stop()
        AndroidLogger.logger.info("\nStopping Appium Espresso stop at port $DEFAULT_PORT\n")
    }

    fun makeRequestForServerToStop() {
        isStopRequestReceived = true
    }

    companion object {
        private const val DEFAULT_PORT = 6791
        val instance = Server()
    }
}
