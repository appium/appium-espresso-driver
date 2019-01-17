package io.appium.espressoserver.test.model

import androidx.test.espresso.action.GeneralLocation.*
import androidx.test.espresso.action.Press.PINPOINT
import androidx.test.espresso.action.Press.THUMB
import androidx.test.espresso.action.Swipe.FAST
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import io.appium.espressoserver.lib.model.MobileSwipeParams.Direction.DOWN
import io.appium.espressoserver.lib.model.MobileSwipeParams.MobileSwipeActionParamsDeserializer
import org.junit.Test
import kotlin.test.assertEquals

class `mobile swipe test` {

    @Test
    fun `should parse "MobileSwipeActionParams" with 'direction' property` () {
        val jsonElement = JsonObject()
        jsonElement.add("direction", JsonPrimitive("DOWN"))
        val mobileSwipeActionParams = MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(mobileSwipeActionParams.direction, DOWN)
    }

    @Test
    fun `should parse "MobileSwipeActionParams" with 'swiper' property and use default parameters` () {
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
    fun `should parse "MobileSwipeActionParams" with 'swiper' property plus other parameters` () {
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

    @Test(expected = JsonParseException::class)
    fun `should reject if both 'direction' and 'swiper' provided`() {
        val jsonElement = JsonObject()
        jsonElement.add("swiper", JsonPrimitive("FAST"))
        jsonElement.add("direction", JsonPrimitive("DOWN"))
        MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
    }

    @Test(expected = JsonParseException::class)
    fun `should reject if do not provided 'direction' or 'swiper` () {
        val jsonElement = JsonObject()
        MobileSwipeActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
    }
}