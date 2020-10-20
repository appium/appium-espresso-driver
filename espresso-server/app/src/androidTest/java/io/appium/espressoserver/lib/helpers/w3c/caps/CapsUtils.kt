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

package io.appium.espressoserver.lib.helpers.w3c.caps

import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException


val STANDARD_CAPS = listOf(
        "browserName",
        "browserVersion",
        "platformName",
        "acceptInsecureCerts",
        "pageLoadStrategy",
        "proxy",
        "setWindowRect",
        "timeouts",
        "unhandledPromptBehavior"
)
const val APPIUM_PREFIX = "appium"


fun isStandardCap(capName: String): Boolean = STANDARD_CAPS.any { it.toLowerCase() == capName.toLowerCase() }


fun mergeCaps(primary: Map<String, Any?>, secondary: Map<String, Any?>): Map<String, Any?> {
    val result = primary.toMutableMap()
    secondary.mapValues {
        if (result[it.key] != null) {
            throw InvalidArgumentException(
                    "Property '${it.key}' should not exist on both primary ($primary) and secondary ($secondary) objects")
        }
        result[it.key] = it.value
    }
    return result
}


fun stripPrefixes(caps: Map<String, Any?>): Map<String, Any?> {
    val prefix = "$APPIUM_PREFIX:"
    val badPrefixedCaps = caps.keys
            .filter { it.startsWith(prefix) }
            .fold(mutableListOf<String>(), { acc, capName ->
                val strippedName = capName.substring(prefix.length)
                if (isStandardCap(strippedName)) {
                    acc.add(strippedName)
                }
                acc
            })
    if (badPrefixedCaps.isNotEmpty()) {
        throw InvalidArgumentException(
                "The capabilities $badPrefixedCaps are standard capabilities and should not have the '$prefix' prefix")
    }

    return caps.map {
        if (it.key.startsWith(prefix) && it.key.length > prefix.length)
            it.key.substring(prefix.length) to it.value
        else
            it.key to it.value
    }.toMap()
}


fun parseCapabilities(firstMatchValue: List<Map<String, Any?>>?,
                      alwaysMatchValue: Map<String, Any?>?): Map<String, Any?> {
    val alwaysMatch = alwaysMatchValue ?: mapOf()
    val firstMatch = firstMatchValue ?: listOf()

    val allFirstMatchCaps = if (firstMatch.isNotEmpty()) firstMatch else listOf<Map<*, *>>(mapOf<String, Any?>())
    val requiredCaps = stripPrefixes(alwaysMatch)
    @Suppress("UNCHECKED_CAST")
    (allFirstMatchCaps as List<Map<String, Any?>>)
            .map { stripPrefixes(it) }
            .forEach {
                try {
                    return mergeCaps(requiredCaps, it)
                } catch (e: Exception) {
                    AndroidLogger.warn(e)
                }
            }
    throw InvalidArgumentException("Could not find matching capabilities from {$firstMatch, $alwaysMatch}")
}
