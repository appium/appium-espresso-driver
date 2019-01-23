package io.appium.espressoserver.lib.model

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import org.hamcrest.Matcher
import java.lang.reflect.Type

@JsonAdapter(DataMatcherJson.DataMatcherJsonDeserializer::class)
data class DataMatcherJson(val matcher:Matcher<*>) : AppiumParams() {

    fun invoke(): DataInteraction {
        return onData(matcher);
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
}