package io.appium.espressoserver.lib.model

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onData
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import org.hamcrest.Matcher
import java.lang.reflect.Type

@JsonAdapter(DataMatcherJsonDeserializer::class)
data class DataMatcherJson(
    val matcher:Matcher<*>
) : AppiumParams() {

    fun invoke(): DataInteraction {
        return onData(matcher);
    }

    companion object {
        fun fromJson(selector:String): DataMatcherJson {
            try {
                return Gson().fromJson(selector, DataMatcherJson::class.java)
            } catch (e: AppiumException) {
                throw InvalidSelectorException(String.format("Not a valid selector '%s'. Reason: '%s'", selector, e.cause))
            } catch (e: JsonParseException) {
                throw InvalidSelectorException(String.format("Could not parse selector '%s'. Reason: '%s'", selector, e.cause))
            }
        }
    }
}

class DataMatcherJsonDeserializer : JsonDeserializer<DataMatcherJson> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, paramType: Type?,
                             paramJsonDeserializationContext: JsonDeserializationContext?): DataMatcherJson {
        if (!json.isJsonObject) {
            throw JsonParseException("Data matcher must be an object. Found '${json}'")
        }

        val matcher = HamcrestMatcher.HamcrestMatcherDeserializer()
                .deserialize(json, null, null)
                .invoke();

        return DataMatcherJson(matcher)
    }
}