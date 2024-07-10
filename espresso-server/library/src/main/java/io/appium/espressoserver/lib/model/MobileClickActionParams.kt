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

import androidx.test.espresso.action.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import java.lang.reflect.Type

@JsonAdapter(MobileClickActionParams.MobileClickActionParamsDeserializer::class)
class MobileClickActionParams : AppiumParams() {
    var tapper : Tapper? = null
    var coordinatesProvider : CoordinatesProvider? = null
    var precisionDescriber : PrecisionDescriber? = null
    var inputDevice = 0
    var buttonState = 0


    class MobileClickActionParamsDeserializer : JsonDeserializer<MobileClickActionParams> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): MobileClickActionParams {
            val clickActionParams = MobileClickActionParams()
            val jsonObject = json.asJsonObject

            if (jsonObject.has("inputDevice")) {
                clickActionParams.inputDevice = jsonObject.get("inputDevice").asInt
            }

            if (jsonObject.has("buttonState")) {
                clickActionParams.buttonState = jsonObject.get("buttonState").asInt
            }

            // Deserialize TAPPER as a tap enum
            clickActionParams.tapper = GsonParserHelpers.parseEnum<Tap>(
                    jsonObject,
                    "tapper",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Tap for list of valid tapper types",
                    Tap.SINGLE
            )

            // Deserialize COORDINATES_PROVIDER as a general location enum
            clickActionParams.coordinatesProvider = GsonParserHelpers.parseEnum<GeneralLocation>(
                    jsonObject,
                    "coordinatesProvider",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/GeneralLocation for list of valid coordinatesProvider types",
                    GeneralLocation.VISIBLE_CENTER
            )

            // Deserialize PRECISION_DESCRIBER as a 'Press' enum
            clickActionParams.precisionDescriber = GsonParserHelpers.parseEnum<Press>(
                    jsonObject,
                    "precisionDescriber",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Press for list of valid precisionDescriber types",
                    Press.FINGER
            )

            return clickActionParams
        }
    }

}
