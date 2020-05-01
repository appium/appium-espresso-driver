package io.appium.espressoserver.lib.helpers.w3c.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class OriginDeserializer : JsonDeserializer<Origin> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, paramType: Type,
                             paramJsonDeserializationContext: JsonDeserializationContext): Origin {
        val origin = Origin()
        if (json.isJsonPrimitive) {
            origin.type = json.asString
        } else if (json.isJsonObject &&
                json.asJsonObject[InputSource.Action.ELEMENT_CODE].isJsonPrimitive) {
            val elementId = json.asJsonObject[InputSource.Action.ELEMENT_CODE].asString
            origin.elementId = elementId
            origin.type = InputSource.Action.ELEMENT_CODE
        } else if (json.isJsonNull) {
            origin.type = VIEWPORT
        }
        return origin
    }
}