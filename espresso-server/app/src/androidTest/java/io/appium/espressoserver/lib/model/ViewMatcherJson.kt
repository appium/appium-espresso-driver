package io.appium.espressoserver.lib.model

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.lang.reflect.Type

@JsonAdapter(ViewMatcherJsonDeserializer::class)
data class ViewMatcherJson(
        val matcher: Matcher<View>
) : AppiumParams() {

    fun invoke(): ViewInteraction {
        return onView(matcher)
    }

    companion object {
        fun fromJson(selector: String): ViewMatcherJson {
            try {
                return Gson().fromJson(selector, ViewMatcherJson::class.java)
            } catch (e: AppiumException) {
                throw InvalidSelectorException(String.format("Not a valid selector '%s'. Reason: '%s'", selector, e.cause))
            } catch (e: JsonParseException) {
                throw InvalidSelectorException(String.format("Could not parse selector '%s'. Reason: '%s'", selector, e.cause))
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ViewMatcherJsonDeserializer : JsonDeserializer<ViewMatcherJson> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, paramType: Type?,
                             paramJsonDeserializationContext: JsonDeserializationContext?): ViewMatcherJson {
        if (!json.isJsonObject) {
            throw JsonParseException("View matcher must be an object. Found '${json}'")
        }

        val matcher = HamcrestMatcher.HamcrestMatcherDeserializer()
                .deserialize(json, null, null).invoke()

        return ViewMatcherJson(matcher as Matcher<View>)

    }
}