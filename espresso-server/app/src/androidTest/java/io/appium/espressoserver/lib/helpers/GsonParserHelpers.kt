package io.appium.espressoserver.lib.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

class GsonParserHelpers {

    inline fun <reified T : Enum<T>> parseEnum(jsonObj: JsonObject, propName: String, helperMessage: String = ""): T? {
        val direction = jsonObj.get(propName)
        if (direction != null) {
            val propValueAsString = jsonObj.get(propName).asString.toUpperCase()
            try {
                return enumValueOf<T>(propValueAsString)
            } catch (e: Exception) {
                throw JsonParseException(""""
                    '${propValueAsString}' is not a valid '${propName}' type. ${helperMessage}
                """.trimIndent());
            }
        }
        return null
    }
}
