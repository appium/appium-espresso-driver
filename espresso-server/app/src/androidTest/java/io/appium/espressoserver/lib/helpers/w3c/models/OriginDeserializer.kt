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
        } else if (json.isJsonObject && json.asJsonObject[Origin.ELEMENT].isJsonPrimitive) {
            val elementId = json.asJsonObject[Origin.ELEMENT].asString
            origin.elementId = elementId
            origin.type = Origin.ELEMENT
        } else if (json.isJsonNull) {
            origin.type = Origin.VIEWPORT
        }
        return origin
    }
}