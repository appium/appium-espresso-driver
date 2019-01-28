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
import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.helpers.GsonParserHelpers
import java.lang.reflect.Type

@JsonAdapter(MobileSwipeParams.MobileSwipeActionParamsDeserializer::class)
class MobileSwipeParams : AppiumParams() {
    var direction: Direction? = null
    var swiper: Swiper? = null
    var startCoordinates: CoordinatesProvider? = null
    var endCoordinates: CoordinatesProvider? = null
    var precisionDescriber: PrecisionDescriber? = null


    enum class Direction {
        @SerializedName("down")
        DOWN,
        @SerializedName("up")
        UP,
        @SerializedName("left")
        LEFT,
        @SerializedName("right")
        RIGHT
    }

    class MobileSwipeActionParamsDeserializer : JsonDeserializer<MobileSwipeParams> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, paramType: Type?,
                                 paramJsonDeserializationContext: JsonDeserializationContext?): MobileSwipeParams {
            val swipeActionParams = MobileSwipeParams()
            val jsonObject = json.asJsonObject


            // Deserialize 'direction'
            swipeActionParams.direction = GsonParserHelpers.parseEnum<Direction>(
                    jsonObject,
                    "direction",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Swipe for list of valid tapper types"
            );

            // Deserialize 'swiper'
            swipeActionParams.swiper = GsonParserHelpers.parseEnum<Swipe>(
                    jsonObject,
                    "swiper",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Swipe for list of valid tapper types"
            )

            // Validate that exactly one of direction or swiper is set
            if (swipeActionParams.direction != null && swipeActionParams.swiper != null) {
                throw JsonParseException("Cannot set both 'direction' and 'swiper' for swipe action. Must set only one");
            } else if (swipeActionParams.direction == null && swipeActionParams.swiper == null) {
                throw JsonParseException("Must set one of 'direction' or 'swiper'");
            }

            // Deserialize 'startCoordinates'
            swipeActionParams.startCoordinates = GsonParserHelpers.parseEnum<GeneralLocation>(
                    jsonObject,
                    "startCoordinates",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/GeneralLocation for list of valid coordinate types",
                    GeneralLocation.BOTTOM_CENTER
            )

            // Deserialize 'endCoordinates'
            swipeActionParams.endCoordinates = GsonParserHelpers.parseEnum<GeneralLocation>(
                    jsonObject,
                    "endCoordinates",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/GeneralLocation for list of valid coordinate types",
                    GeneralLocation.TOP_CENTER
            )

            // Deserialize 'precisionDescriber'
            swipeActionParams.precisionDescriber = GsonParserHelpers.parseEnum<Press>(
                    jsonObject,
                    "precisionDescriber",
                    "See https://developer.android.com/reference/android/support/test/espresso/action/Press for list of valid precision types",
                    Press.THUMB
            )

            return swipeActionParams
        }
    }
}
