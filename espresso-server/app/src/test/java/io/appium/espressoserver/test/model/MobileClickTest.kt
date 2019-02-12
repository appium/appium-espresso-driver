package io.appium.espressoserver.test.model

import androidx.test.espresso.action.GeneralLocation.TOP_RIGHT
import androidx.test.espresso.action.GeneralLocation.VISIBLE_CENTER
import androidx.test.espresso.action.Press.FINGER
import androidx.test.espresso.action.Press.PINPOINT
import androidx.test.espresso.action.Tap.DOUBLE
import androidx.test.espresso.action.Tap.SINGLE
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.appium.espressoserver.lib.model.MobileClickActionParams
import org.junit.Test
import kotlin.test.assertEquals

class `mobile clickAction test` {

    @Test
    fun `should parse "MobileClickParams" and set defaults if some params not provided` () {
        val jsonElement = JsonObject()
        val clickActionParams = MobileClickActionParams.MobileClickActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(clickActionParams.inputDevice, 0)
        assertEquals(clickActionParams.buttonState, 0)
        assertEquals(clickActionParams.tapper, SINGLE)
        assertEquals(clickActionParams.coordinatesProvider, VISIBLE_CENTER)
        assertEquals(clickActionParams.precisionDescriber, FINGER)
    }

    @Test
    fun `should parse "MobileClickParams" and set values if all params provided` () {
        val jsonElement = JsonObject()
        jsonElement.add("inputDevice", JsonPrimitive(2))
        jsonElement.add("buttonState", JsonPrimitive("3"))
        jsonElement.add("tapper", JsonPrimitive("DOUBLE"))
        jsonElement.add("coordinatesProvider", JsonPrimitive("TOP_RIGHT"))
        jsonElement.add("precisionDescriber", JsonPrimitive("PINPOINT"))
        val clickActionParams = MobileClickActionParams.MobileClickActionParamsDeserializer()
                .deserialize(jsonElement, null, null)
        assertEquals(clickActionParams.inputDevice, 2)
        assertEquals(clickActionParams.buttonState, 3)
        assertEquals(clickActionParams.tapper, DOUBLE)
        assertEquals(clickActionParams.coordinatesProvider, TOP_RIGHT)
        assertEquals(clickActionParams.precisionDescriber, PINPOINT)

    }
}