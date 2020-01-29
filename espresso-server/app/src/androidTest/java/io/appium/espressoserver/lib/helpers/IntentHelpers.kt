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

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.reflect.KClass
import kotlin.reflect.full.cast


private fun addFlags(intent: Intent, flags: String) {
    for (flagStr in flags.split(",")) {
        @Suppress("DEPRECATION")
        when (flagStr.trim().toUpperCase()) {
            "FLAG_GRANT_READ_URI_PERMISSION", "GRANT_READ_URI_PERMISSION" ->
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            "FLAG_GRANT_WRITE_URI_PERMISSION", "GRANT_WRITE_URI_PERMISSION" ->
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            "FLAG_EXCLUDE_STOPPED_PACKAGES", "EXCLUDE_STOPPED_PACKAGES" ->
                intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
            "FLAG_INCLUDE_STOPPED_PACKAGES", "INCLUDE_STOPPED_PACKAGES" ->
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            "FLAG_DEBUG_LOG_RESOLUTION", "DEBUG_LOG_RESOLUTION" ->
                intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION)
            "FLAG_ACTIVITY_BROUGHT_TO_FRONT", "ACTIVITY_BROUGHT_TO_FRONT" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            "FLAG_ACTIVITY_CLEAR_TOP", "ACTIVITY_CLEAR_TOP" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET", "ACTIVITY_CLEAR_WHEN_TASK_RESET" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS", "ACTIVITY_EXCLUDE_FROM_RECENTS" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY", "ACTIVITY_LAUNCHED_FROM_HISTORY" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
            "FLAG_ACTIVITY_MULTIPLE_TASK", "ACTIVITY_MULTIPLE_TASK" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            "FLAG_ACTIVITY_NO_ANIMATION", "ACTIVITY_NO_ANIMATION" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            "FLAG_ACTIVITY_NO_HISTORY", "ACTIVITY_NO_HISTORY" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            "FLAG_ACTIVITY_NO_USER_ACTION", "ACTIVITY_NO_USER_ACTION" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            "FLAG_ACTIVITY_PREVIOUS_IS_TOP", "ACTIVITY_PREVIOUS_IS_TOP" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            "FLAG_ACTIVITY_REORDER_TO_FRONT", "ACTIVITY_REORDER_TO_FRONT" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED", "ACTIVITY_RESET_TASK_IF_NEEDED" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            "FLAG_ACTIVITY_SINGLE_TOP", "ACTIVITY_SINGLE_TOP" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            "FLAG_ACTIVITY_CLEAR_TASK", "ACTIVITY_CLEAR_TASK" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            "FLAG_ACTIVITY_TASK_ON_HOME", "ACTIVITY_TASK_ON_HOME" ->
                intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
            "FLAG_RECEIVER_REGISTERED_ONLY", "RECEIVER_REGISTERED_ONLY" ->
                intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
            "FLAG_RECEIVER_REPLACE_PENDING", "RECEIVER_REPLACE_PENDING" ->
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
            else -> {
                throw IllegalArgumentException("The flag '$flagStr' is not known")
            }
        }
    }
}


private data class Pair<T>(val k: String, val v: T)


private fun <T : Any> requirePair(expectedType: KClass<T>, key: String, value: Any?): Pair<T> {
    require(value is List<*> && value.size > 1 && value[0] is String) {
        "The value of '$key' must be a list with at least two items. " +
                "The first item must be a key of type string. '$value' is given instead"
    }
    // It might be that GSON serializer converts all ints to long/floats to double by default
    var v = value[1]
    if (expectedType == Int::class && v is Long) {
        v = v.toInt()
    }
    if (expectedType == Float::class && v is Double) {
        v = v.toFloat()
    }
    require(expectedType.isInstance(v)) {
        "The second item of '$key' must be of type ${expectedType.simpleName}. " +
                "'$value' is given instead"
    }
    return Pair(value[0] as String, expectedType.cast(v))
}


private fun requireString(key: String, value: Any?): String {
    require(value is String) {
        "The value of '$key' must be of type string. '$value' is given instead"
    }
    return value
}


private fun String.toComponentName(): ComponentName = ComponentName.unflattenFromString(this)
        ?: throw IllegalArgumentException("Bad component name: $this")


/**
 * Creates an intent using the given options
 *
 * @param options intent options mapping. The mapping can have the following keys:
 * - `action`: An action name, such as ACTION_VIEW. Application-specific
 *             actions should be prefixed with the vendor's package name.
 * - `data`: Intent data URI, such as content://contacts/people/1
 * - `type`: Intent MIME type, such as image/png
 * - `category`: Intent category, such as android.intent.category.APP_CONTACTS
 * - `component`: Component name with package name prefix to create an explicit intent,
 * such as com.example.app/.ExampleActivity
 * - `intFlags`: Single string value, which represents intent flags set encoded
 * into an integer. Could also be provided in hexadecimal format, such as 0x0F.
 * See https://developer.android.com/reference/android/content/Intent.html#setFlags(int)
 * for more details.
 * - `flags`: Comma-separated string of intent flag names, such as
 * 'FLAG_GRANT_READ_URI_PERMISSION, ACTIVITY_CLEAR_TASK' (the 'FLAG_' prefix
 * could be omitted).
 * - `className`: The name of a class inside of the application package
 * that will be used as the component for this Intent.
 * - `e`, `es`: String data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', 'bar']
 * - `esn`: null-data, where the key name is only needed, such as 'foo'
 * - `ez`: Boolean data as a key-value pair stored in a list of two
 * elements of type string/boolean, such as ['foo', true]
 * - `ei`: Integer data as a key-value pair stored in a list of two
 * elements of type string/integer, such as ['foo', 1]
 * - `el`: Long integer data as a key-value pair stored in a list of two
 * elements of type string/longint, such as ['foo', 1L]
 * - `ef`: Float data as a key-value pair stored in a list of two
 * elements of type string/float, such as ['foo', 1.1]
 * - `eu`: URI data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', 'content://contacts/people/1']
 * - `ecn`: Component name data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', 'com.example.app/.ExampleActivity']
 * - `eia`: Array of integers data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', '1,2,3,4']
 * - `ela`: Array of long integers data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', '1L,2L,3L,4L']
 * - `efa`: Array of float data as a key-value pair stored in a list of two
 * elements of type string, such as ['foo', '1.1,2.2,3.2,4.4']
 * @return The created intent
 * @throws IllegalArgumentException if required options are missing or
 * there is an issue with mapping value format
 *
 */
fun makeIntent(options: Map<String, Any?>): Intent {
    val intent = Intent()
    var hasIntentInfo = false
    var data: Uri? = null
    var type: String? = null

    val handlersMapping = mapOf<String, (key: String, value: Any?) -> Unit>(
            "action" to fun(key, value) {
                intent.action = requireString(key, value)
                hasIntentInfo = true
            },
            "data" to fun(key, value) {
                data = Uri.parse(requireString(key, value))
                hasIntentInfo = true
            },
            "type" to fun(key, value) {
                type = requireString(key, value)
                hasIntentInfo = true
            },
            "category" to fun(key, value) {
                intent.addCategory(requireString(key, value))
                hasIntentInfo = true
            },
            "className" to fun(key, value) {
                intent.setClassName(InstrumentationRegistry.getInstrumentation().targetContext,
                        requireString(key, value))
            },
            "component" to fun(key, value) {
                intent.component = requireString(key, value).toComponentName()
                hasIntentInfo = true
            },
            "intFlags" to fun(key, value) {
                intent.flags = Integer.decode(requireString(key, value)).toInt()
            },
            "flags" to fun(key, value) {
                addFlags(intent, requireString(key, value))
            },
            "e" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                intent.putExtra(k, v)
            },
            "es" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                intent.putExtra(k, v)
            },
            "esn" to fun(key, value) {
                val k = requireString(key, value)
                intent.putExtra(k, null as String?)
            },
            "ei" to fun(key, value) {
                val (k, v) = requirePair(Int::class, key, value)
                intent.putExtra(k, v)
            },
            "eu" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                intent.putExtra(k, Uri.parse(v))
            },
            "el" to fun(key, value) {
                val (k, v) = requirePair(Long::class, key, value)
                intent.putExtra(k, v)
            },
            "ef" to fun(key, value) {
                val (k, v) = requirePair(Float::class, key, value)
                intent.putExtra(k, v)
            },
            "ez" to fun(key, value) {
                val (k, v) = requirePair(Boolean::class, key, value)
                intent.putExtra(k, v)
            },
            "eia" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                val numbers = v.split(",")
                        .map {
                            try {
                                it.trim().toInt()
                            } catch (e: NumberFormatException) {
                                throw IllegalArgumentException(
                                        "The value is expected to be a comma-separated list of integers. " +
                                                "'$v' is given instead", e)
                            }
                        }
                        .toIntArray()
                intent.putExtra(k, numbers)
            },
            "ela" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                val numbers = v.split(",")
                        .map {
                            try {
                                it.trim().toLong()
                            } catch (e: NumberFormatException) {
                                throw IllegalArgumentException(
                                        "The value is expected to be a comma-separated list of long integers. " +
                                                "'$v' is given instead", e)
                            }
                        }
                        .toLongArray()
                intent.putExtra(k, numbers)
            },
            "efa" to fun(key, value) {
                val (k, v) = requirePair(String::class, key, value)
                val numbers = v.split(",")
                        .map {
                            try {
                                it.trim().toFloat()
                            } catch (e: NumberFormatException) {
                                throw IllegalArgumentException(
                                        "The value is expected to be a comma-separated list of float numbers. " +
                                                "'$v' is given instead", e)
                            }
                        }
                        .toFloatArray()
                intent.putExtra(k, numbers)
            },
            "ecn" to fun(key, value) {
                val (k, name) = requirePair(String::class, key, value)
                intent.putExtra(k, name.toComponentName())
            }
    )

    for ((optName, optValue) in options) {
        val handler = handlersMapping[optName]
                ?: throw IllegalArgumentException("The option named '$optName' is not known. " +
                        "Only the following options are supported: ${handlersMapping.keys}")
        handler(optName, optValue)
    }

    intent.setDataAndType(data, type)

    require(hasIntentInfo) { "Either intent action, data, type or category must be supplied" }
    return intent
}
