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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive

object GsonParserHelpers {

    inline fun <reified T : Enum<T>> parseEnum(jsonObj: JsonObject, propName: String,
                                               helperMessage: String = "", defaultValue: T? = null): T? {
        val property = jsonObj.get(propName)
        if (property != null) {
            val propValueAsString = property.asString.toUpperCase()
            try {
                return enumValueOf<T>(propValueAsString)
            } catch (e: IllegalArgumentException) {
                throw JsonParseException(""""
                    '${propValueAsString}' is not a valid '${propName}' type. $helperMessage
                """.trimIndent())
            }
        }
        return defaultValue
    }

    fun parsePrimitive(jsonPrimitive: JsonPrimitive): Any = when {
        jsonPrimitive.isNumber -> {
            val hasDecimal = jsonPrimitive.asString.contains(".")
            if (hasDecimal) {
                jsonPrimitive.asDouble
            } else {
                val candidate = jsonPrimitive.asLong
                if (Int.MIN_VALUE <= candidate && candidate <= Int.MAX_VALUE) {
                    candidate.toInt()
                } else {
                    candidate
                }
            }
        }
        jsonPrimitive.isBoolean -> jsonPrimitive.asBoolean
        jsonPrimitive.isString -> jsonPrimitive.asString
        else -> throw JsonParseException("Could not parse primitive '${jsonPrimitive}'")
    }

    fun asArray (jsonObj: JsonObject, key: String): JsonArray {
        jsonObj.get(key)?.let {
            if (it.isJsonArray) {
                return it.asJsonArray
            }
            val jsonArr = JsonArray()
            jsonArr.add(it)
            return jsonArr
        }
        return JsonArray()
    }
}
