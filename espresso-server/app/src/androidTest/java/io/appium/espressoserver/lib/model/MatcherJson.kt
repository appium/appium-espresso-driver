package io.appium.espressoserver.lib.model

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import java.lang.reflect.Type

fun String.toJsonMatcher(): MatcherJson {
    try {
        return Gson().fromJson(this, MatcherJson::class.java)
    } catch (e: AppiumException) {
        throw InvalidSelectorException(String.format("Not a valid selector '%s'. Reason: '%s'", this, e.cause), e)
    } catch (e: JsonParseException) {
        throw InvalidSelectorException(String.format("Could not parse selector '%s'. Reason: '%s'", this, e.cause), e)
    }
}

@JsonAdapter(MatcherJsonDeserializer::class)
data class MatcherJson(val query: ScopedMatcher) : AppiumParams()

class MatcherJsonDeserializer : JsonDeserializer<MatcherJson> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, paramType: Type?,
                             paramJsonDeserializationContext: JsonDeserializationContext?): MatcherJson {
        if (!json.isJsonObject) {
            throw JsonParseException("Matcher must be an object. Found '${json}'")
        }

        val matcher = HamcrestMatcher.HamcrestMatcherDeserializer()
                .deserialize(json, null, null)
                .invoke()
        return MatcherJson(matcher)
    }
}