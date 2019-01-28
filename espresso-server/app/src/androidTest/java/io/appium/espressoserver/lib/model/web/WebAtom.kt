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

package io.appium.espressoserver.lib.model.web

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import io.appium.espressoserver.lib.model.AppiumParams
import java.lang.reflect.Type

@JsonAdapter(WebAtom.WebAtomDeserializer::class)
data class WebAtom(val name: String, val args: Array<Any> = emptyArray()) : AppiumParams() {
    class WebAtomDeserializer : JsonDeserializer<WebAtom> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): WebAtom {

            if (json.isJsonObject) {
                val jsonObj = json.asJsonObject

                // Parse the name of the Atom
                val name = jsonObj.get("name")

                if (name == null || !name.isJsonPrimitive) {
                    throw JsonParseException("'name' property must be provided to WebAtom and must be a string")
                }

                val webAtomName = name.asString

                // Parse the Locator shorthand. If it was provided.
                jsonObj.get("locator")?.let {
                    // Validate the locator
                    if (!it.isJsonObject) {
                        throw JsonParseException("'locator' must be an object with properties 'using' and 'value'");
                    }

                    val locator = it.asJsonObject;
                    if (!locator.has("using") || !locator.has("value")) {
                        throw JsonParseException("'locator' must have properties 'using' and 'value'");
                    }

                    if (!locator.get("using").isJsonPrimitive || !locator.get("value").isJsonPrimitive) {
                        throw JsonParseException("'using' and 'value' must be primitive types. " +
                                "Found 'using=${locator.get("using")}, value=${locator.get("value")}'")
                    }

                    // Set the args as locator
                    return WebAtom(webAtomName, arrayOf(
                            locator.get("using").asString,
                            locator.get("value").asString
                    ))
                }

                // Parse the args
                jsonObj.get("args")?.let {
                    if (it.isJsonPrimitive) {
                        return WebAtom(webAtomName, arrayOf(it.asString))
                    } else if (it.isJsonArray){
                        val argsAsList = ArrayList<Any>()
                        for (arg in it.asJsonArray) {
                            if (arg.isJsonPrimitive) {
                                argsAsList.add(GsonParserHelpers.parsePrimitive(arg.asJsonPrimitive))
                            } else {
                                throw JsonParseException("'${arg}' is not a valid 'arg' type");
                            }
                        }
                        return WebAtom(webAtomName, argsAsList.toArray())
                    } else {
                        throw JsonParseException("'args' must be an array or a singleton primitive JSON type. Found '${it}' ")
                    }
                }

                // If no args provided, treat it as a function call with no parameters
                if (!jsonObj.has("args")) {
                    return WebAtom(webAtomName, emptyArray());
                }

            } else if (json.isJsonPrimitive) {
                // If JSON was provided as a String, treat it as a function call with no parameters
                return WebAtom(json.asString, emptyArray())
            }

            // This block is unreachable
            throw JsonParseException("Expected atom to be a string or an object. Found '${json}'")
        }
    }

}
