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

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import java.lang.IllegalArgumentException

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
                    '${propValueAsString}' is not a valid '${propName}' type. ${helperMessage}
                """.trimIndent());
            }
        }
        return defaultValue
    }

    fun parsePrimitive(jsonPrimitive: JsonPrimitive):Any {
        if (jsonPrimitive.isNumber) {
            // Gson doesn't have internal way of distinguishing floats from ints... so check string
            val hasDecimal = if (jsonPrimitive.asString.contains(".")) true else false

            if (hasDecimal) {
                return jsonPrimitive.asDouble;
            } else {
                return jsonPrimitive.asLong;
            }
        } else if (jsonPrimitive.isBoolean) {
            return jsonPrimitive.asBoolean;
        } else if (jsonPrimitive.isString) {
            return jsonPrimitive.asString;
        }

        // Should be unreachable. Boolean, string and number should cover the primitives
        throw JsonParseException("Unknown error occurred. Could not parse primitive '${jsonPrimitive}'");
    }
}
