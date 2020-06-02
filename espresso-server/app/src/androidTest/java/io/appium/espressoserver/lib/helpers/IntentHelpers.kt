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
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlin.reflect.KClass
import kotlin.reflect.full.cast


private fun Intent.addFlags(flags: String) {
    for (flagStr in flags.split(",")) {
        @Suppress("DEPRECATION")
        when (flagStr.trim().toUpperCase()) {
            "FLAG_GRANT_READ_URI_PERMISSION", "GRANT_READ_URI_PERMISSION" ->
                this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            "FLAG_GRANT_WRITE_URI_PERMISSION", "GRANT_WRITE_URI_PERMISSION" ->
                this.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            "FLAG_EXCLUDE_STOPPED_PACKAGES", "EXCLUDE_STOPPED_PACKAGES" ->
                this.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
            "FLAG_INCLUDE_STOPPED_PACKAGES", "INCLUDE_STOPPED_PACKAGES" ->
                this.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            "FLAG_DEBUG_LOG_RESOLUTION", "DEBUG_LOG_RESOLUTION" ->
                this.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION)
            "FLAG_ACTIVITY_BROUGHT_TO_FRONT", "ACTIVITY_BROUGHT_TO_FRONT" ->
                this.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            "FLAG_ACTIVITY_CLEAR_TOP", "ACTIVITY_CLEAR_TOP" ->
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET", "ACTIVITY_CLEAR_WHEN_TASK_RESET" ->
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS", "ACTIVITY_EXCLUDE_FROM_RECENTS" ->
                this.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY", "ACTIVITY_LAUNCHED_FROM_HISTORY" ->
                this.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
            "FLAG_ACTIVITY_MULTIPLE_TASK", "ACTIVITY_MULTIPLE_TASK" ->
                this.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            "FLAG_ACTIVITY_NO_ANIMATION", "ACTIVITY_NO_ANIMATION" ->
                this.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            "FLAG_ACTIVITY_NO_HISTORY", "ACTIVITY_NO_HISTORY" ->
                this.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            "FLAG_ACTIVITY_NO_USER_ACTION", "ACTIVITY_NO_USER_ACTION" ->
                this.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            "FLAG_ACTIVITY_PREVIOUS_IS_TOP", "ACTIVITY_PREVIOUS_IS_TOP" ->
                this.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            "FLAG_ACTIVITY_REORDER_TO_FRONT", "ACTIVITY_REORDER_TO_FRONT" ->
                this.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED", "ACTIVITY_RESET_TASK_IF_NEEDED" ->
                this.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            "FLAG_ACTIVITY_SINGLE_TOP", "ACTIVITY_SINGLE_TOP" ->
                this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            "FLAG_ACTIVITY_NEW_TASK", "ACTIVITY_NEW_TASK" ->
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            "FLAG_ACTIVITY_CLEAR_TASK", "ACTIVITY_CLEAR_TASK" ->
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            "FLAG_ACTIVITY_TASK_ON_HOME", "ACTIVITY_TASK_ON_HOME" ->
                this.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
            "FLAG_RECEIVER_REGISTERED_ONLY", "RECEIVER_REGISTERED_ONLY" ->
                this.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
            "FLAG_RECEIVER_REPLACE_PENDING", "RECEIVER_REPLACE_PENDING" ->
                this.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
            else -> throw IllegalArgumentException("The flag '${flagStr.trim()}' is not known")
        }
    }
}

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
 * - `categories`: One or more comma-separated Intent categories,
 * such as android.intent.category.APP_CONTACTS
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
 * - `e`, `es`: String data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': 'bar'}
 * - `esn`: null-data, where the key name is only needed, such as ['foo', 'bar']
 * - `ez`: Boolean data as key-value pairs stored in a map with
 * keys and values of type string->boolean, such as {'foo': true, 'bar': false}
 * - `ei`: Integer data as key-value pairs stored in a map with
 * keys and values of type string->integer, such as {'foo': 1, 'bar': 2}
 * - `el`: Long integer data as key-value pairs stored in a map with
 * keys and values of type string->long, such as {'foo': 1L, 'bar': 2L}
 * - `ef`: Float data as key-value pairs stored in a map with
 * keys and values of type string->float, such as {'foo': 1.ff, 'bar': 2.2f}
 * - `eu`: URI data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': 'content://contacts/people/1'}
 * - `ecn`: Component name data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': 'com.example.app/.ExampleActivity'}
 * - `eia`: Array of integers data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': '1,2,3,4'}
 * - `ela`: Array of long data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': '1L,2L,3L,4L'}
 * - `efa`: Array of float data as key-value pairs stored in a map with
 * keys and values of type string->string, such as {'foo': '1.1,2.2,3.2,4.4'}
 * @return The created intent
 * @throws IllegalArgumentException if required options are missing or
 * there is an issue with mapping value format
 */
fun makeIntent(context: Context?, options: Map<String, Any?>): Intent {
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
            "categories" to fun(key, value) {
                requireString(key, value).split(",")
                        .forEach { intent.addCategory(it.trim()) }
                hasIntentInfo = true
            },
            "className" to fun(key, value) {
                intent.setClassName(context!!, requireString(key, value))
            },
            "component" to fun(key, value) {
                intent.component = requireString(key, value).toComponentName()
                hasIntentInfo = true
            },
            "intFlags" to fun(key, value) {
                intent.flags = Integer.decode(requireString(key, value)).toInt()
            },
            "flags" to fun(key, value) {
                intent.addFlags(requireString(key, value))
            },
            "e" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireString(it.key as String, it.value))
                        }
            },
            "es" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireString(it.key as String, it.value))
                        }
            },
            "esn" to fun(key, value) {
                requireList(key, value)
                        .filterIsInstance<String>()
                        .forEach { intent.putExtra(it, null as String?) }
            },
            "ei" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireInt(it.key as String, it.value))
                        }
            },
            "eu" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, Uri.parse(requireString(it.key as String, it.value)))
                        }
            },
            "el" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireLong(it.key as String, it.value))
                        }
            },
            "ef" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireFloat(it.key as String, it.value))
                        }
            },
            "ez" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String, requireBool(it.key as String, it.value))
                        }
            },
            "eia" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach { entry ->
                            requireString(entry.key as String, entry.value)
                                    .split(",")
                                    .map {
                                        try {
                                            it.trim().toInt()
                                        } catch (e: NumberFormatException) {
                                            throw IllegalArgumentException(
                                                    "The value of '${entry.key}' is expected " +
                                                            "to be a comma-separated list of integers. " +
                                                            "'${entry.value}' is given instead", e)
                                        }
                                    }
                                    .toIntArray()
                                    .let { intent.putExtra(entry.key as String, it) }
                        }
            },
            "ela" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach { entry ->
                            requireString(entry.key as String, entry.value)
                                    .split(",")
                                    .map {
                                        try {
                                            it.trim().toLong()
                                        } catch (e: NumberFormatException) {
                                            throw IllegalArgumentException(
                                                    "The value of '${entry.key}' is expected " +
                                                            "to be a comma-separated list of long integers. " +
                                                            "'${entry.value}' is given instead", e)
                                        }
                                    }
                                    .toLongArray()
                                    .let { intent.putExtra(entry.key as String, it) }
                        }
            },
            "efa" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach { entry ->
                            requireString(entry.key as String, entry.value)
                                    .split(",")
                                    .map {
                                        try {
                                            it.trim().toFloat()
                                        } catch (e: NumberFormatException) {
                                            throw IllegalArgumentException(
                                                    "The value of '${entry.key}' is expected " +
                                                            "to be a comma-separated list of float numbers. " +
                                                            "'${entry.value}' is given instead", e)
                                        }
                                    }
                                    .toFloatArray()
                                    .let { intent.putExtra(entry.key as String, it) }
                        }
            },
            "ecn" to fun(key, value) {
                requireMap(key, value)
                        .filter { it.key is String }
                        .forEach {
                            intent.putExtra(it.key as String,
                                    requireString(it.key as String, it.value).toComponentName())
                        }
            }
    )

    for ((optName, optValue) in options) {
        val handler = handlersMapping[optName]
                ?: throw IllegalArgumentException("The option named '$optName' is not known. " +
                        "Only the following options are supported: ${handlersMapping.keys}")
        handler(optName, optValue)
    }

    intent.setDataAndType(data, type)

    require(hasIntentInfo) { "Either intent action, data, type or categories must be supplied" }
    return intent
}

fun extractQualifiedClassName(pkg: String, fullName: String): String {
    var className = fullName
    val slashPos = className.indexOf("/")
    if (slashPos >= 0 && className.length > slashPos) {
        className = className.substring(slashPos + 1)
    }
    return if (className.startsWith(".")) "${pkg}${className}" else className
}
