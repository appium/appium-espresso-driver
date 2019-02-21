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

package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException


val SESSION_ID_PARAM_NAME = "sessionId"
val ELEMENT_ID_PARAM_NAME = "targetElement"

open class AppiumParams {
    private var uriParams: MutableMap<String, String> = mutableMapOf()

    val sessionId: String?
        get() = getUriParameterValue(SESSION_ID_PARAM_NAME)

    var elementId: String?
        get() = getUriParameterValue(ELEMENT_ID_PARAM_NAME) ?:
            throw InvalidArgumentException("No element found in query string")
        set(elementId) = setUriParameterValue(ELEMENT_ID_PARAM_NAME, elementId ?:
            throw AppiumException("Cannot set elementId to null"))

    fun initUriMapping(params: Map<String, String>) {
        uriParams.clear()
        uriParams.putAll(params)
    }

    fun getUriParameterValue(name: String): String? {
        return uriParams[name]
    }

    private fun setUriParameterValue(name: String, value: String) {
        uriParams[name] = value
        return
    }
}
