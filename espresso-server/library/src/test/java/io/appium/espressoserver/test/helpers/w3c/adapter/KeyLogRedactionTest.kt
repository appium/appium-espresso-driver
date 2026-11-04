package io.appium.espressoserver.test.helpers.w3c.adapter

import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyLogRedactionTest {
    @Test
    fun keyEventLogDoesNotContainTypedCharacters() {
        val event = W3CKeyEvent().apply {
            key = "ж"
            code = "KeyJ"
        }
        val message = event.logMessage()
        assertFalse(message.contains("ж"))
        assertFalse(message.contains("KeyJ"))
    }

    @Test
    fun keyEventLogKeepsSpecialKeysApart() {
        val event = W3CKeyEvent().apply { key = "" }
        assertTrue(event.logMessage().contains("key=[special]"))
    }

    @Test
    fun keyInputStateLogDoesNotContainPressedKeys() {
        val state = KeyInputState().apply { addPressed("Q") }
        assertFalse(state.logMessage().contains("Q"))
        assertTrue(state.logMessage().contains("pressedCount=[1]"))
    }
}
