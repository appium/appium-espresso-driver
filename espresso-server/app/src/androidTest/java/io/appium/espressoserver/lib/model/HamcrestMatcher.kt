package io.appium.espressoserver.lib.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

@JsonAdapter(HamcrestMatcher.HamcrestMatcherDeserializer::class)
data class HamcrestMatcher (var name:String, var args:List<Any?>) {

    class HamcrestMatcherDeserializer : JsonDeserializer<HamcrestMatcher> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): HamcrestMatcher {

            if (json.isJsonObject) {
                val name: String;
                val args = ArrayList<Any?>();

                val jsonObj = json.asJsonObject

                // Parse the name property
                val nameProp = jsonObj.get("name")
                if (nameProp != null && nameProp.isJsonPrimitive) {
                    name = jsonObj.get("name").asString
                } else {
                    throw JsonParseException("Matcher must contain 'name' property");
                }

                // Parse args property
                val argsProp = jsonObj.get("args")
                if (argsProp != null) {
                    var listOfArgs: Iterable<JsonElement> = Collections.emptyList();
                    if (argsProp.isJsonPrimitive || argsProp.isJsonObject || argsProp.isJsonNull) {
                        listOfArgs = Collections.singletonList(argsProp)
                    } else if (argsProp.isJsonArray) {
                        listOfArgs = argsProp.asJsonArray
                    }

                    for (arg in listOfArgs) {
                        if (arg.isJsonPrimitive) {
                            val argPrimitive = arg.asJsonPrimitive;
                            if (argPrimitive.isString) args.add(argPrimitive.asString)
                            else if (argPrimitive.isNumber) args.add(argPrimitive.asNumber)
                            else if (argPrimitive.isBoolean) args.add(argPrimitive.asBoolean)
                        } else if (arg.isJsonNull) {
                            args.add(null)
                        } else if (arg.isJsonObject) {
                            args.add(HamcrestMatcherDeserializer().deserialize(arg, null, null))
                        }
                    }
                }
                return HamcrestMatcher(name, args);
            } else if (json.isJsonPrimitive) {
                // If it's just a primitive, return that as the name and no args
                return HamcrestMatcher(json.asString, Collections.emptyList());
            }

            throw JsonParseException("Matcher must be a JSON object with a 'name' property (required) and 'args' property");
        }
    }
}