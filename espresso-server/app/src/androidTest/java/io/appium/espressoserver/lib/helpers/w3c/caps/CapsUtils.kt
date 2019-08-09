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

package io.appium.espressoserver.lib.helpers

import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException


val STANDARD_CAPS = listOf(
        'browserName',
        'browserVersion',
        'platformName',
        'acceptInsecureCerts',
        'pageLoadStrategy',
        'proxy',
        'setWindowRect',
        'timeouts',
        'unhandledPromptBehavior',
        )
val APPIUM_PREFIX = 'appium'


fun isStandardCap(capName: String): Boolean = STANDARD_CAPS.find { it.toLowerCase() == capName.toLowerCase() }


fun mergeCaps(primary: Map<String, Any?>, secondary: Map<String, Any?>): Map<String, Any?> {
    val result = secondary.toMutableMap()
    secondary.mapValues {
        primary[it.key] ?: throw InvalidArgumentException(
                "Property '${it.key}' should not exist on both primary ($primary) and secondary ($secondary) objects")
        result [it.key] = it.value
    }
    return result
}


fun stripPrefixes(caps: MutableMap<String, Any?>): Map<String, Any?> {
    val prefix = "$APPIUM_PREFIX:"
    val prefixedCaps = caps.keys.filter { it.startsWith(prefix) }

    val badPrefixedCaps = prefixedCaps.fold(mutableListOf<String>(), { acc, capName ->
        val strippedName = capName.substring(prefix.length)
        if (isStandardCap(strippedName)) {
            acc.add(strippedName)
        }
        caps[strippedName] = caps[capName]
        caps.delete(capName)
        acc
    })

    if (!badPrefixedCaps.isEmpty()) {
        throw InvalidArgumentException(
                "The capabilities ${badPrefixedCaps} are standard capabilities and should not have the '$prefix' prefix")
    }

    return caps
}


fun parseCapabilities(caps: Map<String, Any?>): Map<String, Any?> {
    val requiredCaps = caps['alwaysMatch'] ?: mutableMapOf<String, Any?>()
    val allFirstMatchCaps = caps['firstMatch'] ?: mutableListOf<Map<String, Any?>>()

    if (allFirstMatchCaps !is List<String, Any?>) {
        throw InvalidArgumentException(
                'The capabilities.firstMatch argument was not valid for the following reason: '
                '"capabilities.firstMatch" must be a JSON array or unset. '
                "Got '$allFirstMatchCaps' instead"
        )
    }

    if (allFirstMatchCaps.isEmpty()) {
        allFirstMatchCaps.add(mapOf<String, Any?>())
    }

    stripPrefixes(requiredCaps)
    allFirstMatchCaps
            .map { stripPrefixes(it) }
            .forEach {
                try {
                    return mergeCaps(requiredCaps, it)
                } catch (Exception: e) {
                    AndroidLogger.logger.warn(e)
                }
            }
    throw InvalidArgumentException("Could not find matching capabilities from $caps")
}
