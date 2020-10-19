package io.appium.espressoserver.test.helpers.w3c

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.dispatchKeyDown
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.dispatchKeyUp
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyDispatchTest {
    @Test
    @Throws(AppiumException::class)
    fun shouldToggleAltIfAltPassedToBlankKeyState() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val alt = "\uE00A"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = alt
        val keyInputState = KeyInputState()
        assertFalse(keyInputState.isAlt)
        assertFalse(keyInputState.isCtrl)
        val cancelList = inputStateTable.cancelList
        assertTrue(cancelList.isEmpty())
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable)
        assertEquals(cancelList.size, 1)
        val cancelObject = cancelList[0]
        assertEquals(cancelObject.subType, InputSource.ActionType.KEY_UP)
        assertEquals(cancelObject.value, alt)
        assertTrue(keyInputState.isAlt)
        assertFalse(keyInputState.isCtrl)
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldAddKeysToKeyPressedState() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val value = "F"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = value
        val keyInputState = KeyInputState()
        assertFalse(keyInputState.isPressed(value))
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable)
        assertTrue(keyInputState.isPressed(value))
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldPassKeyCodeToEvent() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = "F"
        val keyInputState = KeyInputState()
        val keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable)
        assertEquals(keyEvent!!.code, "KeyF")
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldPassNullKeyCodeAndLocationZeroIfNotDefined() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = "\uFFFF"
        val keyInputState = KeyInputState()
        val keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable)
        assertNull(keyEvent!!.code)
        assertEquals(keyEvent.location, 0)
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldReleaseAltOnAltKeyUp() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val alt = "\uE00A"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = alt
        val keyInputState = KeyInputState()
        keyInputState.addPressed("ALT")
        keyInputState.isAlt = true
        keyInputState.isCtrl = true
        assertTrue(keyInputState.isAlt)
        assertTrue(keyInputState.isCtrl)
        dispatchKeyUp(adapter, actionObject, keyInputState, inputStateTable)
        assertFalse(keyInputState.isAlt)
        assertTrue(keyInputState.isCtrl)
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldAddAndRemoveKeysFromKeyPressedState() {
        val adapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val id = "keyboard"
        val value = "~"
        val actionObject = ActionObject(id, InputSourceType.KEY, InputSource.ActionType.KEY_DOWN, 0)
        actionObject.value = value
        val keyInputState = KeyInputState()
        assertFalse(keyInputState.isPressed(value))

        // Press down a key and check that it's pressed
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable)
        assertTrue(keyInputState.isPressed(value))

        // Release a key and check that it's not pressed
        dispatchKeyUp(adapter, actionObject, keyInputState, inputStateTable)
        assertFalse(keyInputState.isPressed(value))
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldNormalizeKeys() {
        val normalizedKey = KeyNormalizer.toNormalizedKey("\uE008")
        assertEquals(normalizedKey, NormalizedKeys.SHIFT)
    }
}