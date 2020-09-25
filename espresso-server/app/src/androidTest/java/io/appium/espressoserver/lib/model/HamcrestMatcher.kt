/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import io.appium.espressoserver.lib.helpers.ReflectionUtils.invokeStaticMethod
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.lang.reflect.Type
import kotlin.reflect.KClass

const val CLASS_SUFFIX = ".class"

@JsonAdapter(HamcrestMatcher.HamcrestMatcherDeserializer::class)
data class HamcrestMatcher(var name: String, var args: Array<Any?>, var matcherClass: KClass<*> = Matchers::class) {

    private fun argsWithTypes(): List<*> {
        return this.args
                .map {
                    if (it !is String) {
                        return@map it
                    }

                    var className = it
                    if (it.endsWith(CLASS_SUFFIX)) {
                        className = className.take(className.length - CLASS_SUFFIX.length)
                    }

                    try {
                        Class.forName(className)
                    } catch (e: ClassNotFoundException) {
                        try {
                            Class.forName("java.lang.${className}")
                        } catch (e: ClassNotFoundException) {
                            it
                        }
                    }
        }
    }

    fun invoke(): Matcher<*> {
        val typedArgs = this.argsWithTypes().toTypedArray()
        val matcher = try {
            invokeStaticMethod(this.matcherClass.java, this.name, *typedArgs)
        } catch (e: NoSuchMethodException) {
            throw InvalidSelectorException("${matcherClass.qualifiedName} does not implement any $name " +
                    "methods that accept $typedArgs argument(s)", e)
        }
        if (matcher !is Matcher<*>) {
            throw InvalidArgumentException("'${this}' does not return a Matcher when invoked. " +
                    "Found '${matcher?.let { it::class.qualifiedName } ?: "null"}'")
        }
        return matcher
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
                val listOfArgs = GsonParserHelpers.asArray(jsonObj, "args")

                val args = arrayListOf<Any?>()
                for (arg in listOfArgs) {
                    when {
                        arg.isJsonPrimitive -> args.add(GsonParserHelpers.parsePrimitive(arg.asJsonPrimitive))
                        arg.isJsonNull -> args.add(null)
                        arg.isJsonObject -> args.add(HamcrestMatcherDeserializer().deserialize(arg, null, null).invoke())
                    }
                }

                // Parse the 'class' property
                jsonObj.get("class")?.let {
                    if (it.isJsonPrimitive) {
                        val className = it.asString

                        // Try fully casting class as fully qualified name
                        try {
                            val matcherClass = Class.forName(className).kotlin
                            return HamcrestMatcher(name, args.toTypedArray(), matcherClass)
                        } catch (cnfe: ClassNotFoundException) {
                        }

                        // If above didn't work, try prepending 'androidx.test.espresso.matcher' package name
                        try {
                            val qualifiedClassName = "androidx.test.espresso.matcher.${className}"
                            val matcherClass = Class.forName(qualifiedClassName).kotlin
                            return HamcrestMatcher(name, args.toTypedArray(), matcherClass)
                        } catch (cnfe: ClassCastException) {
                            throw JsonParseException("No such class found '${className}'")
                        }
                    }

                    throw JsonParseException("'matcherClass' must be a string. Found '${it}'")
                }

                return HamcrestMatcher(name, args.toTypedArray())
            } else if (json.isJsonPrimitive) {
                // If it's just a primitive, return that as the name and no args
                return HamcrestMatcher(json.asString, emptyArray())
            }

            throw JsonParseException("Matcher must be a JSON object with a 'name' property (required) " +
                    "and optional 'args' property and 'matcherClass' property")
        }
    }
}