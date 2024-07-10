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
import kotlin.reflect.full.cast

/**
 * Creates an options using the given activityOptions
 *
 * @param activityOptions mapping. The mapping can have the following keys:
 * - `launchDisplayId`: display id which you want to launch, such as {"launchDisplayId":"1"}
 * @return The created options
 * @throws IllegalArgumentException if required options are missing or
 * there is an issue with mapping value format
 */

fun makeActivityOptions(activityOptions: Map<String, Any?>?): ActivityOptions {
    val options = ActivityOptions.makeBasic()
    val handlersMapping = mapOf<String, (key: String, value: Any?) -> Unit>(
            "launchDisplayId" to fun(_, value) {
                try {
                    if (String::class.isInstance(value)) {
                        options.launchDisplayId = String::class.cast(value).toInt()
                    } else if (Number::class.isInstance(value)) {
                        options.launchDisplayId = Number::class.cast(value).toInt()
                    } else {
                        throw IllegalArgumentException(
                                "The value of 'launchDisplayId' must be of type 'String' or 'Number'. '$value' is given instead")
                    }
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException(
                            "Can't covert '$value' to Int", e)
                }
            }
    )
    activityOptions?.let {
        for ((optName: String, optValue: Any?) in it.entries) {
            val handler = handlersMapping[optName]
                    ?: throw IllegalArgumentException("The option named '$optName' is not known. " +
                            "Only the following options are supported: ${handlersMapping.keys}")
            handler(optName, optValue)
        }
    }
    return options
}
