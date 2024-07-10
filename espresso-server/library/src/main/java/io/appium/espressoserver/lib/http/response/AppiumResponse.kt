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

package io.appium.espressoserver.lib.http.response

import android.util.Log
import java.util.UUID
import fi.iki.elonen.NanoHTTPD.Response.Status
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.MESSAGE_UNKNOWN_ERROR

class AppiumResponse : BaseResponse {
    internal var value: Any? = null
    var sessionId: String? = null
        private set
    // Unique Appium transaction ID
    var id: String? = null
        private set

    constructor(value: Any?) {
        init(value, null)
    }

    constructor(value: Any?, sessionId: String?) {
        init(value, sessionId)
    }

    private fun formatError(e: AppiumException,
                            originalStacktrace: String): Map<String, String> = mapOf(
        "error" to e.error(),
        "message" to (e.message ?: MESSAGE_UNKNOWN_ERROR),
        "stacktrace" to originalStacktrace
    )

    private fun init(value: Any?, sessionId: String?) {
        if (value is Throwable) {
            val e = if (value is AppiumException) value else AppiumException(value)
            this.value = formatError(e, Log.getStackTraceString(value))
            this.httpStatus = e.status()
        } else {
            this.value = value
            this.httpStatus = Status.OK
        }
        this.sessionId = sessionId
        this.id = UUID.randomUUID().toString()
    }
}

