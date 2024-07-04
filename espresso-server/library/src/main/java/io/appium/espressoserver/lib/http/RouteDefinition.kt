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

import java.util.HashMap

import fi.iki.elonen.NanoHTTPD.Method
import io.appium.espressoserver.lib.handlers.RequestHandler
import io.appium.espressoserver.lib.model.AppiumParams

class RouteDefinition(val method: Method, val routeUri: String,
                      val handler: RequestHandler<*, *>,
                      val paramClass: Class<out AppiumParams>) {

    private val testRegex = buildTestRegex(routeUri)

    fun isMatch(uri: String): Boolean = uri.matches(testRegex)

    fun getUriParams(uri: String): Map<String, String> {
        val uriParams = HashMap<String, String>()
        val uriTokens = uri.split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for ((index, routeUriToken) in routeUri.split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
                .withIndex()) {
            // If a token starts with ':', then what's after is an identifier
            if (routeUriToken.startsWith(":")) {
                uriParams[routeUriToken.substring(1)] = uriTokens[index]
            }
        }
        return uriParams
    }

    private fun buildTestRegex(uri: String): Regex {
        val testRegex = StringBuilder("^")

        // Convert route to a regex
        for (uriToken in uri.split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
            if (uriToken.startsWith(":")) {
                testRegex.append("/[^/]*")
            } else if (uriToken.isNotEmpty()) {
                testRegex.append("/")
                testRegex.append(uriToken)
            }
        }
        testRegex.append("/?$")
        return testRegex.toString().toRegex()
    }
}
