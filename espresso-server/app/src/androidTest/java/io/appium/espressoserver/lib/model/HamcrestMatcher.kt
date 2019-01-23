package io.appium.espressoserver.lib.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import org.hamcrest.Matcher
import java.lang.ClassCastException
import java.lang.reflect.Type
import kotlin.reflect.KClass

@JsonAdapter(HamcrestMatcher.HamcrestMatcherDeserializer::class)
data class HamcrestMatcher (var name:String, var args:List<Any?>, var matcherClass:KClass<*> = org.hamcrest.Matchers::class) {

    fun invoke():Matcher<*> {
        throw Exception("Not ready yet")
    }

    class HamcrestMatcherDeserializer : JsonDeserializer<HamcrestMatcher> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): HamcrestMatcher {
            
            if (json.isJsonObject) {

                val jsonObj = json.asJsonObject

                // Validate and parse the name property
                val nameProp = jsonObj.get("name")

                if (nameProp == null) {
                    throw JsonParseException("Matcher must contain 'name' property")
                } else if (!nameProp.isJsonPrimitive) {
                    throw JsonParseException("'name' property on matcher must be a JSON primitive")
                }

                val name = nameProp.asString

                // Parse args property
                val args = arrayListOf<Any?>()
                jsonObj.get("args")?.let {
                    var listOfArgs: Iterable<JsonElement> = emptyList()
                    if (it.isJsonPrimitive || it.isJsonObject || it.isJsonNull) {
                        listOfArgs = arrayListOf(it)
                    } else if (it.isJsonArray) {
                        listOfArgs = it.asJsonArray
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

                // Parse the matcherClass property
                jsonObj.get("class")?.let {
                    if (it.isJsonPrimitive) {
                        val className = it.asString

                        // Try fully casting class as fully qualified name
                        try {
                            val matcherClass = Class.forName(className).kotlin;
                            return HamcrestMatcher(name, args, matcherClass)
                        } catch (cnfe: ClassNotFoundException) { }

                        // If above didn't work, try prepending 'androidx.test.espresso.matcher' package name
                        try {
                            val matcherClass = Class.forName("androidx.test.espresso.matcher.${className}").kotlin
                            return HamcrestMatcher(name, args, matcherClass)
                        } catch (cnfe: ClassCastException) {
                            throw JsonParseException("No such class found '${className}'")
                        }
                    } else {
                        throw JsonParseException("'matcherClass' must be a string. Found '${it}'")
                    }
                }

                return HamcrestMatcher(name, args)
            } else if (json.isJsonPrimitive) {
                // If it's just a primitive, return that as the name and no args
                return HamcrestMatcher(json.asString, emptyList());
            }

            throw JsonParseException("Matcher must be a JSON object with a 'name' property (required) " +
                    "and optional 'args' property and 'matcherClass' property");
        }
    }
}