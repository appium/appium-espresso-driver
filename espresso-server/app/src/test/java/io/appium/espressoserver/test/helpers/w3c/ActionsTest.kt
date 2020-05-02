package io.appium.espressoserver.test.helpers.w3c

import com.google.gson.Gson
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState
import io.appium.espressoserver.test.assets.readAssetFile
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class ActionsTest {
    internal class AlteredDummyAdapter : DummyW3CActionAdapter() {
        // Bump up viewport width so we don't get out of bounds issues
        override val viewportWidth: Long
            get() =// Bump up viewport width so we don't get out of bounds issues
                300

        // Bump up viewport width so we don't get out of bounds issues
        override val viewportHeight: Long
            get() =// Bump up viewport width so we don't get out of bounds issues
                500
    }

    @Test
    @Throws(IOException::class)
    fun shouldThrowIfAdapterNotSet() {
        val multiTouchJson = readAssetFile("multi-touch-actions.json")
        val actions = Gson().fromJson(multiTouchJson, Actions::class.java)
        try {
            actions.perform("123")
        } catch (e: AppiumException) {
            TestCase.assertTrue(e.message!!.contains("Failed to initialize /actions adapter"))
        }
    }

    @Test
    @Throws(IOException::class, AppiumException::class)
    fun shouldPerformPointerActionsOnASetOfInputSources() {
        val multiTouchJson = readAssetFile("multi-touch-actions.json")
        val actions = Gson().fromJson(multiTouchJson, Actions::class.java)
        actions.adapter = AlteredDummyAdapter()
        val sessionId = "123"
        actions.perform(sessionId)
        val inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId)
        val finger1 = inputStateTable.getInputState("finger1") as PointerInputState
        val finger2 = inputStateTable.getInputState("finger2") as PointerInputState

        // Check the state
        assertFloatEquals(finger1.x, 120f)
        assertFloatEquals(finger1.y, 100f)
        assertFloatEquals(finger2.x, 250f)
        assertFloatEquals(finger2.y, 400f)

        // Sanity check that it's recording pointer move events
        val pointerMoveEvents = (actions.adapter as DummyW3CActionAdapter?)!!.getPointerMoveEvents()
        TestCase.assertTrue(pointerMoveEvents.isNotEmpty())
    }

    @Test
    @Throws(IOException::class, AppiumException::class)
    fun shouldPerformKeyActionsOnASetOfInputSources() {
        val keyJson = readAssetFile("key-actions.json")
        val actions = Gson().fromJson(keyJson, Actions::class.java)
        actions.adapter = AlteredDummyAdapter()
        val sessionId = "123"
        actions.perform(sessionId)
        val inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId)
        val keyboard = inputStateTable.getInputState("keyboard") as KeyInputState
        Assert.assertFalse(keyboard.isPressed("\\uE009"))
    }
}