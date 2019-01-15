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
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger.logger
import java.lang.reflect.Type

@JsonAdapter(MobileSwipeParams.MobileSwipeActionParamsDeserializer::class)
class MobileSwipeParams : AppiumParams() {
    var direction: Direction? = null
    var swiper: Swiper? = null
    var startCoordinates = GeneralLocation.BOTTOM_CENTER
    var endCoordinates = GeneralLocation.TOP_CENTER
    var precisionDescriber = Press.THUMB


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
        override fun deserialize(json: JsonElement, paramType: Type,
                                 paramJsonDeserializationContext: JsonDeserializationContext): MobileSwipeParams {
            val swipeActionParams = MobileSwipeParams()
            val jsonObject = json.asJsonObject

            // Deserialize SWIPER as swipe enum
            val direction = jsonObject.get("direction")
            if (direction != null) {
                val directionAsString = jsonObject.get("direction").asString.toUpperCase()
                logger.info("Swiper as string ${directionAsString}")
                try {
                    swipeActionParams.direction = Direction.valueOf(directionAsString)
                } catch (e: Exception) {
                    throw InvalidArgumentException(""""
                    '${directionAsString}' is not a valid 'swiper' type. See
                    https://developer.android.com/reference/android/support/test/espresso/action/Swipe
                    for list of valid tapper types
                """.trimIndent());
                }
            }

            // Deserialize SWIPER as swipe enum
            val swiper = jsonObject.get("swiper")
            if (swiper != null) {
                val swiperAsString = jsonObject.get("swiper").asString.toUpperCase()
                logger.info("Swiper as string ${swiperAsString}")
                try {
                    swipeActionParams.swiper = Swipe.valueOf(swiperAsString)
                } catch (e: Exception) {
                    throw InvalidArgumentException(""""
                    '${swiperAsString}' is not a valid 'swiper' type. See
                    https://developer.android.com/reference/android/support/test/espresso/action/Swipe
                    for list of valid tapper types
                """.trimIndent());
                }
            }

            // Validate that exactly one of direction or swiper is set
            if (swipeActionParams.direction != null && swipeActionParams.swiper != null) {
                throw InvalidArgumentException("Cannot set both 'direction' and 'swiper' for swipe action. Must set only one");
            } else if (swipeActionParams.direction == null && swipeActionParams.swiper == null) {
                throw InvalidArgumentException("Must set one of 'direction' or 'swiper'");
            }

            // Deserialize TAPPER as a tap enum
            if (jsonObject.has("startCoordinates")) {
                val startCoordinatesAsString = jsonObject.get("startCoordinates").asString.toUpperCase()
                try {
                    swipeActionParams.startCoordinates = GeneralLocation.valueOf(startCoordinatesAsString);
                } catch (e: Exception) {
                    throw InvalidArgumentException(""""
                    '${startCoordinatesAsString}' is not a valid 'startCoordinates' type. See
                    https://developer.android.com/reference/android/support/test/espresso/action/Tap
                    for list of valid tapper types
                """.trimIndent());
                }
            }

            // Deserialize COORDINATES_PROVIDER as a general location enum
            if (jsonObject.has("endCoordinates")) {
                val endCoordinatesAsString = jsonObject.get("endCoordinates").asString.toUpperCase()
                try {
                    swipeActionParams.endCoordinates = GeneralLocation.valueOf(endCoordinatesAsString)
                } catch (e: Exception) {
                    throw InvalidArgumentException(""""
                    '${endCoordinatesAsString}' is not a valid 'endCoordinates' type. See
                    https://developer.android.com/reference/android/support/test/espresso/action/GeneralLocation
                    for list of valid coordinates types
                """.trimIndent());
                }
            }

            // Deserialize PRECISION_DESCRIBER as a 'Press' enum
            if (jsonObject.has("precisionDescriber")) {
                val precisionDescriberAsString = jsonObject.get("precisionDescriber").asString.toUpperCase()
                try {
                    swipeActionParams.precisionDescriber = Press.valueOf(precisionDescriberAsString);
                } catch (e: Exception) {
                    throw InvalidArgumentException(""""
                    ${precisionDescriberAsString} is not a valid 'precisionDescriber' type. See
                    https://developer.android.com/reference/android/support/test/espresso/action/Tap
                    for list of valid 'precisionDescriber' types
                """.trimIndent());
                }
            }

            return swipeActionParams
        }
    }
}
