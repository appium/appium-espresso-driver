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

import android.app.ActivityOptions
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

private fun <T : Any> requireType(expectedType: KClass<T>, key: String, value: Any?): T {
    var v = value
    // It might be that GSON serializer converts all ints to long/floats to double by default
    if (expectedType == Int::class && v is Long) {
        v = v.toInt()
    }
    if (expectedType == Float::class && v is Double) {
        v = v.toFloat()
    }
    require(expectedType.isInstance(v)) {
        "The value of '$key' must be of type '${expectedType.simpleName}'. '$value' is given instead"
    }
    return expectedType.cast(v)
}

private fun requireString(key: String, value: Any?): String = requireType(String::class, key, value)
private fun requireInt(key: String, value: Any?): Int = requireType(Int::class, key, value)
private fun requireLong(key: String, value: Any?): Long = requireType(Long::class, key, value)
private fun requireFloat(key: String, value: Any?): Float = requireType(Float::class, key, value)
private fun requireBool(key: String, value: Any?): Boolean = requireType(Boolean::class, key, value)
private fun requireMap(key: String, value: Any?): Map<*, *> = requireType(Map::class, key, value)
private fun requireList(key: String, value: Any?): List<*> = requireType(List::class, key, value)

/**
 * Creates an options using the given activityOptions
 *
 * @param activityOptions mapping. The mapping can have the following keys:
 * - `launchDisplayId`: display id which you want to launch, such as {"launchDisplayId":"1"}
 * @return The created intent
 * @throws IllegalArgumentException if required options are missing or
 * there is an issue with mapping value format
 */

fun makeActivityOptions(activityOptions: Map<String, Any?>): ActivityOptions {
    val options = ActivityOptions.makeBasic()
    val handlersMapping = mapOf<String, (key: String, value: Any?) -> Unit>(
            "launchDisplayId" to fun(key, value) {
                options.launchDisplayId = requireString(key, value).toInt()
            }
    )
    for ((optName, optValue) in activityOptions) {
        val handler = handlersMapping[optName]
                ?: throw IllegalArgumentException("The option named '$optName' is not known. " +
                        "Only the following options are supported: ${handlersMapping.keys}")
        handler(optName, optValue)
    }

    return options
}
