package io.appium.espressoserver.lib.model.gsonparsers

import com.google.gson.JsonObject
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger

class GsonParserHelpers {

    inline fun <reified T : Enum<T>> parseEnum(jsonObj: JsonObject, propName: String, helperMessage: String = ""): T? {
        val direction = jsonObj.get(propName)
        if (direction != null) {
            val propValueAsString = jsonObj.get(propName).asString.toUpperCase()
            try {
                return enumValueOf<T>(propValueAsString)
            } catch (e: Exception) {
                throw InvalidArgumentException(""""
                    '${propValueAsString}' is not a valid 'swiper' type. ${helperMessage}
                """.trimIndent());
            }
        }
        return null
    }
}
