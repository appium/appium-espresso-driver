package io.appium.espressoserver.test.model

import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralLocation.*
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Press.PINPOINT
import androidx.test.espresso.action.Press.THUMB
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.Swipe.FAST
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import io.appium.espressoserver.lib.model.MobileSwipeParams
import io.appium.espressoserver.lib.model.MobileSwipeParams.Direction.DOWN
import io.appium.espressoserver.lib.model.MobileSwipeParams.MobileSwipeActionParamsDeserializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MobileSwipeTest {

    @Test
    fun shouldParseMobileSwipeParamsWithDirection () {
        val jsonElement = JsonObject()
        jsonElement.add("direction", JsonPrimitive("DOWN"))
        val mobileSwipeActionParams = MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(mobileSwipeActionParams.direction, DOWN)
    }

    @Test
    fun shouldParseMobileSwipeParamsWithSwiper () {
        val jsonElement = JsonObject()
        jsonElement.add("swiper", JsonPrimitive("FAST"))
        val mobileSwipeActionParams = MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(mobileSwipeActionParams.swiper, FAST);
        assertEquals(mobileSwipeActionParams.startCoordinates, BOTTOM_CENTER);
        assertEquals(mobileSwipeActionParams.endCoordinates, TOP_CENTER);
        assertEquals(mobileSwipeActionParams.precisionDescriber, THUMB);
    }

    @Test
    fun shouldParseMobileSwipeParamsWithSwiperAndSetParams () {
        val jsonElement = JsonObject()
        jsonElement.add("swiper", JsonPrimitive("FAST"))
        jsonElement.add("startCoordinates", JsonPrimitive("TOP_LEFT"))
        jsonElement.add("endCoordinates", JsonPrimitive("TOP_RIGHT"))
        jsonElement.add("precisionDescriber", JsonPrimitive("PINPOINT"))
        val mobileSwipeActionParams = MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(mobileSwipeActionParams.swiper, FAST);
        assertEquals(mobileSwipeActionParams.startCoordinates, TOP_LEFT);
        assertEquals(mobileSwipeActionParams.endCoordinates, TOP_RIGHT);
        assertEquals(mobileSwipeActionParams.precisionDescriber, PINPOINT);
    }

    @Test
    fun shouldRejectIfDirectionAndSwiperProvided () {
        try {
            val jsonElement = JsonObject()
            jsonElement.add("swiper", JsonPrimitive("FAST"))
            jsonElement.add("direction", JsonPrimitive("DOWN"))
            MobileSwipeActionParamsDeserializer()
                    .deserialize(jsonElement, null, null)
        } catch (jpe:JsonParseException) {
            return assertTrue(true);
        }
        assertTrue(false);
    }

    @Test
    fun shouldRejectIfNotDirectionOrSwiperProvided () {
        try {
            val jsonElement = JsonObject()
            MobileSwipeActionParamsDeserializer()
                    .deserialize(jsonElement, null, null)
        } catch (jpe:JsonParseException) {
            return assertTrue(true);
        }
        assertTrue(false);
    }
}