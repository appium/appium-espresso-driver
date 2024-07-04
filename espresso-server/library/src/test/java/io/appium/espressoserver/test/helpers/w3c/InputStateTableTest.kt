package io.appium.espressoserver.test.helpers.w3c

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputStateTableTest {
    @Test
    @Throws(AppiumException::class)
    fun shouldUndoPointerDownEvents() {
        val inputStateTable = InputStateTable()
        val adapter: W3CActionAdapter = DummyW3CActionAdapter()

        // Create a pointer down action
        val actionObject = ActionObject()
        actionObject.type = InputSourceType.POINTER
        actionObject.subType = InputSource.ActionType.POINTER_DOWN
        actionObject.button = 5
        actionObject.id = "123"

        // Call the pointer down action
        actionObject.dispatch(adapter, inputStateTable, 0f, 0)

        // Check that the button is depressed
        val inputState = inputStateTable.getInputState("123") as PointerInputState?
        assertTrue(inputState!!.buttons.contains(5))

        // Undo the action and check that it is not depressed anymore
        inputStateTable.undoAll(adapter, 0)
        assertFalse(inputState.buttons.contains(5))
        assertTrue(inputState.buttons.isEmpty())
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldUndoKeyDownEvents() {
        val inputStateTable = InputStateTable()
        val adapter: W3CActionAdapter = DummyW3CActionAdapter()
        val SHIFT = "\uE008"

        // Create a pointer down action
        val actionObject = ActionObject()
        actionObject.type = InputSourceType.KEY
        actionObject.subType = InputSource.ActionType.KEY_DOWN
        actionObject.value = SHIFT
        actionObject.id = "456"

        // Call the pointer down action
        actionObject.dispatch(adapter, inputStateTable, 0f, 0)

        // Check that the button is depressed
        val normalizedKey = KeyNormalizer.toNormalizedKey(SHIFT)
        val inputState = inputStateTable.getInputState("456") as KeyInputState?
        assertTrue(inputState!!.isPressed(normalizedKey))
        assertTrue(inputState.isShift)

        // Undo the action and check that it is not depressed anymore
        inputStateTable.undoAll(adapter, 0)
        assertFalse(inputState.isPressed(normalizedKey))
        assertFalse(inputState.isShift)
    }
}