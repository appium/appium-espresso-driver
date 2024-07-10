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

package io.appium.espressoserver.lib.handlers.exceptions

import fi.iki.elonen.NanoHTTPD

const val MESSAGE_UNKNOWN_ERROR = "unknown error"
val STATUS_UNKNOWN_ERROR = NanoHTTPD.Response.Status.INTERNAL_ERROR

open class AppiumException : Exception {
    constructor() : super("An unknown server-side error occurred while processing the command") {}

    constructor(reason: String) : super(reason) {}

    constructor(e: Throwable) : super(e) {}

    constructor(reason: String, e: Throwable) : super(reason, e) {}

    open fun error(): String {
        return MESSAGE_UNKNOWN_ERROR
    }

    open fun status(): NanoHTTPD.Response.Status {
        return STATUS_UNKNOWN_ERROR
    }
}
