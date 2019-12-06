package io.appium.espressoserver.lib.model

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import java.lang.reflect.Type
import org.hamcrest.Matcher

@JsonAdapter(MatcherJsonDeserializer::class)
data class MatcherJson(
        val matcher: Matcher<*>
) : AppiumParams() {

    companion object {
        fun fromJson(selector: String): MatcherJson {
            try {
                return Gson().fromJson(selector, MatcherJson::class.java)
            } catch (e: AppiumException) {
                throw InvalidSelectorException(String.format("Not a valid selector '%s'. Reason: '%s'", selector, e.cause))
            } catch (e: JsonParseException) {
                throw InvalidSelectorException(String.format("Could not parse selector '%s'. Reason: '%s'", selector, e.cause))
            }
        }
    }
}

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