package io.appium.espressoserver.test.helpers.w3c

import com.google.gson.Gson

import org.junit.Test

import java.io.IOException
import java.util.concurrent.ExecutionException

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.*
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER
import io.appium.espressoserver.test.assets.readAssetFile
import org.junit.Assert.*

class ActionSequenceTest {

    @Test
    @Throws(IOException::class, InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldPullOutPointerActionsInW3CActions() {
        val multiTouchJson = readAssetFile("multi-touch-actions.json")
        val actions = Actions::class.java.cast(Gson().fromJson(multiTouchJson, Actions::class.java))
        val actionSequence = ActionSequence(actions!!, ActiveInputSources(), InputStateTable())

        // Tick #1 of 6
        var tick = actionSequence.next()
        var action = tick.next()
        assertEquals(action.id, "finger1")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_MOVE)
        assertEquals(action.duration, 0f)
        assertFloatEquals(action.x!!, 100f)
        assertFloatEquals(action.y!!, 100f)

        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_MOVE)
        assertEquals(action.duration, 10f)
        assertFloatEquals(action.x!!, 200f)
        assertFloatEquals(action.y!!, 400f)

        assertFalse(tick.hasNext())

        // Tick #2 of 6
        tick = actionSequence.next()
        action = tick.next()
        assertEquals(action.id, "finger1")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_DOWN)
        assertEquals(action.button.toLong(), 0)

        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_DOWN)
        assertEquals(action.button.toLong(), 0)

        // Tick #3 of 6
        tick = actionSequence.next()
        action = tick.next()
        assertEquals(action.id, "finger1")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, PAUSE)
        assertEquals(action.button.toLong(), 0)

        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, PAUSE)
        assertEquals(action.button.toLong(), 0)

        assertFalse(tick.hasNext())

        // Tick #4 of 6
        tick = actionSequence.next()
        action = tick.next()
        assertEquals(action.id, "finger1")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_MOVE)
        assertEquals(action.duration, 1000f)
        assertTrue(action.pointer == PointerType.TOUCH)
        assertFloatEquals(action.x!!, 20f)
        assertFloatEquals(action.y!!, 0f)

        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, POINTER_MOVE)
        assertEquals(action.duration, 1000f)
        assertEquals(action.origin.type, Origin.POINTER)
        assertFloatEquals(action.x!!, 50f)
        assertFloatEquals(action.y!!, 0f)

        assertFalse(tick.hasNext())

        // Tick #5 of 6
        tick = actionSequence.next()
        action = tick.next()
        assertEquals(action.id, "finger1")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, ActionType.POINTER_UP)
        assertEquals(action.button.toLong(), 0)

        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, ActionType.POINTER_UP)
        assertEquals(action.button.toLong(), 0)

        assertFalse(tick.hasNext())

        // Tick #6 of 6
        tick = actionSequence.next()
        action = tick.next()
        assertEquals(action.id, "finger2")
        assertEquals(action.type, POINTER)
        assertEquals(action.subType, PAUSE)
        assertEquals(action.duration, 0f)

        assertFalse(tick.hasNext())
        assertFalse(actionSequence.hasNext())
    }


    @Test
    @Throws(IOException::class, InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldPullOutKeyActionsInW3CActions() {
        val keyJson = readAssetFile("key-actions.json")
        val actions = Actions::class.java.cast(Gson().fromJson(keyJson, Actions::class.java))
        val actionSequence = ActionSequence(actions!!, ActiveInputSources(), InputStateTable())

        var action: ActionObject
        var tick: Tick

        val unicodeChar = '\uE009'.toString()

        val expectedSubTypes = arrayOf(KEY_DOWN, PAUSE, KEY_DOWN, KEY_UP, KEY_UP)
        val expectedValue = arrayOf(unicodeChar, null, "s", unicodeChar, "s")


        for (i in expectedSubTypes.indices) {
            tick = actionSequence.next()
            action = tick.next()
            assertEquals(action.type, KEY)
            assertEquals(action.subType, expectedSubTypes[i])
            assertEquals(action.value, expectedValue[i])
            assertEquals(action.id, "keyboard")
            assertFalse(tick.hasNext())
        }

        assertFalse(actionSequence.hasNext())
    }

    @Test
    @Throws(IOException::class, AppiumException::class, InterruptedException::class, ExecutionException::class)
    fun shouldDispatchW3CPointerActions() {
        class AlteredDummyAdapter : DummyW3CActionAdapter() {
            override// Bump up viewport width so we don't get out of bounds issues
            val viewportWidth: Long
                get() = 251

            override val viewportHeight: Long
                get() = 401
        }

        val inputStateTable = InputStateTable()
        val multiTouchJson = readAssetFile("multi-touch-actions.json")
        val actions = Actions::class.java.cast(Gson().fromJson(multiTouchJson, Actions::class.java))
        val actionSequence = ActionSequence(actions!!, ActiveInputSources(), InputStateTable())

        val timeBefore = System.currentTimeMillis()
        actionSequence.dispatch(AlteredDummyAdapter(), inputStateTable)
        val elapsedTime = System.currentTimeMillis() - timeBefore

        // Must be greater than 2s because that's the total duration
        assertTrue(elapsedTime >= 2000)

        // Check that it's under 2.5s though to verify that it's not TOO long
        assertTrue(elapsedTime < 2500)

        val finger1State = inputStateTable.getInputState("finger1") as PointerInputState
        val finger2State = inputStateTable.getInputState("finger2") as PointerInputState

        assertFloatEquals(finger1State.x, 120f)
        assertFloatEquals(finger1State.y, 100f)
        assertFloatEquals(finger2State.x, 250f)
        assertFloatEquals(finger2State.y, 400f)
    }

    @Test
    @Throws(IOException::class, AppiumException::class, InterruptedException::class, ExecutionException::class)
    fun shouldDispatchW3CKeyActions() {
        val inputStateTable = InputStateTable()
        val multiTouchJson = readAssetFile("key-actions.json")
        val actions = Actions::class.java.cast(Gson().fromJson(multiTouchJson, Actions::class.java))
        val actionSequence = ActionSequence(actions!!, ActiveInputSources(), InputStateTable())

        val timeBefore = System.currentTimeMillis()
        actionSequence.dispatch(DummyW3CActionAdapter(), inputStateTable)
        val elapsedTime = System.currentTimeMillis() - timeBefore
        assertTrue(elapsedTime >= 500)
        assertTrue(elapsedTime <= 600)
    }
}
