package io.appium.espressoserver.test.helpers.w3c.adapter

import android.graphics.Point
import android.view.MotionEvent
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.getToolType
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.toCoordinates
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HelpersTest {
    @Test
    fun toolTypeDefaultIsFinger() {
        Assert.assertEquals(getToolType(null).toLong(), MotionEvent.TOOL_TYPE_FINGER.toLong())
    }

    @Test
    fun toolTypeTest() {
        Assert.assertEquals(getToolType(InputSource.PointerType.MOUSE).toLong(), MotionEvent.TOOL_TYPE_MOUSE.toLong())
        Assert.assertEquals(getToolType(InputSource.PointerType.PEN).toLong(), MotionEvent.TOOL_TYPE_STYLUS.toLong())
        Assert.assertEquals(getToolType(InputSource.PointerType.TOUCH).toLong(), MotionEvent.TOOL_TYPE_FINGER.toLong())
    }

    @Test
    fun toCoordinatesTest() {
        Assert.assertEquals(toCoordinates(123.0f, 456.0f), Point(123, 456))
    }
}