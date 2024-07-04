package io.appium.espressoserver.test.model

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.model.TouchAction
import io.appium.espressoserver.lib.model.TouchAction.TouchActionOptions
import io.appium.espressoserver.test.helpers.w3c.assertFloatEquals
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TouchActionTest {

    @Test
    @Throws(AppiumException::class)
    fun shouldConvertMoveTo() {
        val touchAction = TouchAction()
        touchAction.action = TouchAction.ActionType.MOVE_TO
        val options = TouchActionOptions()
        options.x = 100L
        options.y = 200L
        touchAction.options = options
        val actions = touchAction.toW3CAction()
        Assert.assertEquals(actions[0].type, InputSource.ActionType.PAUSE)
        Assert.assertEquals(actions[1].type, InputSource.ActionType.PAUSE)
        val action = actions[2]
        assertFloatEquals(action.x!!, 100f)
        assertFloatEquals(action.y!!, 200f)
        Assert.assertEquals(action.type, InputSource.ActionType.POINTER_MOVE)
        Assert.assertTrue(action.isOriginViewport)
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldConvertPress() {
        val actionTypes = arrayOf(TouchAction.ActionType.TAP, TouchAction.ActionType.PRESS, TouchAction.ActionType.LONG_PRESS)
        for (actionType in actionTypes) {
            val touchAction = TouchAction()
            touchAction.action = actionType
            val options = TouchActionOptions()
            options.x = 100L
            options.y = 200L
            touchAction.options = options
            val actions = touchAction.toW3CAction()
            val moveAction = actions[0]
            Assert.assertEquals(moveAction.type, InputSource.ActionType.POINTER_MOVE)
            assertFloatEquals(moveAction.x!!, 100f)
            assertFloatEquals(moveAction.y!!, 200f)
            val upAction = actions[1]
            Assert.assertEquals(upAction.type, InputSource.ActionType.POINTER_DOWN)
            assertEquals(upAction.button, 0)
            val waitAction = actions[2]
            Assert.assertEquals(waitAction.type, InputSource.ActionType.PAUSE)
        }
    }
}