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

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.model.AppiumParams
import java.lang.reflect.Type
import java.util.*

@JsonAdapter(WebAtom.WebAtomDeserializer::class)
class WebAtom : AppiumParams() {
    var name: String? = null
    var args: List<Any>? = null

    class WebAtomDeserializer : JsonDeserializer<WebAtom> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): WebAtom {
            val webAtom = WebAtom();

            if (json.isJsonObject) {
                val jsonObj = json.asJsonObject

                // Parse the name of the Web Atom
                val name = jsonObj.get("name")
                if (name != null && name.isJsonPrimitive) {
                    webAtom.name = jsonObj.get("name").asString
                }

                // Parse the args
                val args = jsonObj.get("args")
                if (args != null) {
                    if (args.isJsonPrimitive) {
                        webAtom.args = Collections.singletonList(args.asString)
                    } else if (args.isJsonArray){
                        val argsAsList = ArrayList<Any>()
                        for (arg in args.asJsonArray) {
                            if (arg.isJsonPrimitive) {
                                val argPrimitive = arg.asJsonPrimitive
                                if (argPrimitive.isBoolean) argsAsList.add(argPrimitive.asBoolean)
                                if (argPrimitive.isNumber) argsAsList.add(argPrimitive.asNumber)
                                if (argPrimitive.isString) argsAsList.add(argPrimitive.asString)
                            } else {
                                throw JsonParseException("'${arg}' is not a valid 'arg' type");
                            }
                        }
                        webAtom.args = argsAsList
                    }
                }

                // Parse the Locator shorthand
                val locator = jsonObj.get("locator")
                if (locator != null) {
                    webAtom.args = mutableListOf(
                            locator.asJsonObject.get("using").asString,
                            locator.asJsonObject.get("value").asString
                    )
                }

            } else if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                // If JSON was provided as a String, treat it as a function call with no args
                webAtom.name = json.asString
            }

            return webAtom;
        }
    }

}
