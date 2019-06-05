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

import java.util.UUID
import fi.iki.elonen.NanoHTTPD.Response.Status
import io.appium.espressoserver.lib.model.AppiumStatus

class AppiumResponse<T> : BaseResponse {
    internal var value: T? = null
    var status: AppiumStatus? = null
        private set
    var sessionId: String? = null
        private set
    // Unique Appium transaction ID
    var id: String? = null
        private set

    constructor(status: AppiumStatus, value: T?) {
        init(status, value, null)
    }

    constructor(status: AppiumStatus, value: T?, sessionId: String?) {
        init(status, value, sessionId)
    }

    private fun init(status: AppiumStatus, value: T?, sessionId: String?) {
        this.value = value
        this.status = status
        this.sessionId = sessionId
        id = UUID.randomUUID().toString()

        httpStatus = when (status) {
            AppiumStatus.SUCCESS -> Status.OK
            AppiumStatus.UNKNOWN_COMMAND -> Status.NOT_FOUND
            else -> Status.INTERNAL_ERROR
        }
    }
}

