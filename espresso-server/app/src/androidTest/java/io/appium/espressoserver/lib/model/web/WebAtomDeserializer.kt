package io.appium.espressoserver.lib.model.web

import androidx.test.espresso.web.webdriver.Locator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.helpers.AssertHelpers
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import java.lang.reflect.Type


// Deserializer for WebAtom
class WebAtomDeserializer : JsonDeserializer<WebAtom> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, paramType: Type?,
                             paramJsonDeserializationContext: JsonDeserializationContext?): WebAtom {

        if (json.isJsonObject) {
            val jsonObj = json.asJsonObject

            // Parse the name of the Atom
            AssertHelpers.assertIsPrimitive(jsonObj, "name")
            val webAtomName = jsonObj.get("name").asString

            // Parse the Locator shorthand. If it was provided.
            jsonObj.get("locator")?.let {
                // Validate the locator
                if (!it.isJsonObject) {
                    throw JsonParseException("'locator' must be an object with properties 'using' and 'value'")
                }

                // Validate that the locator contains "using" and "value"
                val locator = it.asJsonObject
                if (!locator.has("using") || !locator.has("value")) {
                    throw JsonParseException("'locator' must have properties 'using' and 'value'")
                }

                // Validate that "using" and "value" are primitives
                val using = locator.get("using")
                val value = locator.get("value")

                if (!using.isJsonPrimitive || !value.isJsonPrimitive) {
                    throw JsonParseException("'using' and 'value' must be primitive types. " +
                            "Found 'using=${locator.get("using")}, value=${locator.get("value")}'")
                }

                val supportedLocatorNames = Locator.values().map { v -> v.name }
                if (using.asString.toUpperCase() !in supportedLocatorNames) {
                    throw InvalidSelectorException("Only the following locator types are supported: " +
                            "$supportedLocatorNames (case-insensitive). '${locator.get("using")}' is given instead")
                }

                // Set the args as locator
                return WebAtom(webAtomName, arrayOf(
                        Locator.valueOf(using.asString.toUpperCase()),
                        value.asString
                ))
            }

            // Parse the args
            jsonObj.get("args")?.let { args ->
                when {
                    args.isJsonPrimitive -> {
                        return WebAtom(webAtomName, arrayOf(GsonParserHelpers.parsePrimitive(args.asJsonPrimitive)))
                    }
                    args.isJsonArray -> {
                        val argsAsList = args.asJsonArray.map { arg ->
                            if (arg.isJsonPrimitive)
                                GsonParserHelpers.parsePrimitive(arg.asJsonPrimitive)
                            else
                                throw JsonParseException("'${arg}' is not a valid 'arg' type")
                        }

                        return WebAtom(webAtomName, argsAsList.toTypedArray())
                    }
                    else -> {
                        throw JsonParseException(
                                "'args' must be an array or a single, primitive JSON type. Found '${args}'"
                        )
                    }
                }
            }

            // If no args provided, treat it as a function call with no parameters
            if (!jsonObj.has("args")) {
                return WebAtom(webAtomName, emptyArray())
            }

        } else if (json.isJsonPrimitive) {
            // If JSON was provided as a String, treat it as a function call with no parameters
            return WebAtom(json.asString, emptyArray())
        }

        // This block is unreachable
        throw JsonParseException("Expected atom to be a string or an object. Found '${json}'")
    }
}