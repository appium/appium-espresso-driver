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
    var tapper = Tap.SINGLE
    var coordinatesProvider = GeneralLocation.VISIBLE_CENTER
    var precisionDescriber = Press.FINGER
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

            val gsonParserHelpers = GsonParserHelpers();

            // Deserialize TAPPER as a tap enum
            val tapper = gsonParserHelpers.parseEnum<Tap>(
                    jsonObject,
                    "tapper",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Tap for list of valid tapper types"
            )
            if (tapper != null) {
                clickActionParams.tapper = tapper;
            }

            // Deserialize COORDINATES_PROVIDER as a general location enum
            val coordinatesProvider = gsonParserHelpers.parseEnum<GeneralLocation>(
                    jsonObject,
                    "coordinatesProvider",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/GeneralLocation for list of valid coordinatesProvider types"
            )
            if (coordinatesProvider != null) {
                clickActionParams.coordinatesProvider = coordinatesProvider;
            }

            // Deserialize PRECISION_DESCRIBER as a 'Press' enum
            val precisionDescriber = gsonParserHelpers.parseEnum<Press>(
                    jsonObject,
                    "precisionDescriber",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Press for list of valid precisionDescriber types"
            )
            if (precisionDescriber != null) {
                clickActionParams.precisionDescriber = precisionDescriber;
            }

            return clickActionParams
        }
    }

}
