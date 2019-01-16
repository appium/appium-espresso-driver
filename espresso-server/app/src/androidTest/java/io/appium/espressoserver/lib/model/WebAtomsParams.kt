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
import androidx.test.espresso.web.webdriver.Locator
import java.lang.reflect.Type
import java.util.*

@JsonAdapter(WebAtomsParams.WebAtomsDeserializer::class)
class WebAtomsParams : AppiumParams() {
    var webviewEl : String? = null
    var elementSelector : Selector? = null
    var elementReferenceAtom: Atom? = null
    var perform: Atom? = null

    class WebAtomsDeserializer : JsonDeserializer<WebAtomsParams> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): WebAtomsParams {

            return WebAtomsParams() // TODO: Do more deserialization here
        }
    }

    data class Selector(val locator: Locator, val value: String)
    data class Atom(val name: String, val args: List<Any> = Collections.emptyList())

}
