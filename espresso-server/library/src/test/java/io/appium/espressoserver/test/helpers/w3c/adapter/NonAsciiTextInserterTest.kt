package io.appium.espressoserver.test.helpers.w3c.adapter

import android.text.InputFilter
import android.view.KeyEvent
import android.text.Selection
import android.text.SpannableStringBuilder
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.AndroidKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.NonAsciiTextInserter.Companion.insertIntoEditable
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.NonAsciiTextInserter.Companion.isTypeableCodePoint
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NonAsciiTextInserterTest {
    private fun editable(text: String, start: Int = -1, end: Int = start) =
            SpannableStringBuilder(text).also { if (start >= 0) Selection.setSelection(it, start, end) }

    private fun keyEvent(value: String) = W3CKeyEvent().also { it.key = value; it.keyCode = -1 }

    @Test
    fun typeableCodePoints() {
        listOf("П", "日", "ी", "😀", " ").forEach {
            Assert.assertTrue(it, isTypeableCodePoint(it.codePointAt(0)))
        }
        listOf(0x0, 0x0A, 0x7F, 0xE000, 0xE017, 0xD800, 0x378, 0x110000).forEach {
            Assert.assertFalse(it.toString(), isTypeableCodePoint(it))
        }
    }

    @Test
    fun appendsWithoutSelection() {
        val e = editable("ab")
        Assert.assertEquals(1, insertIntoEditable(e, "Ж"))
        Assert.assertEquals("abЖ", e.toString())
        Assert.assertEquals(3, Selection.getSelectionEnd(e))
    }

    @Test
    fun insertsAtCursor() {
        val e = editable("abc", 1)
        insertIntoEditable(e, "Ж")
        Assert.assertEquals("aЖbc", e.toString())
        Assert.assertEquals(2, Selection.getSelectionStart(e))
        Assert.assertEquals(2, Selection.getSelectionEnd(e))
    }

    @Test
    fun replacesReversedSelection() {
        val e = editable("abcd", 3, 1)
        insertIntoEditable(e, "Ж")
        Assert.assertEquals("aЖd", e.toString())
        Assert.assertEquals(2, Selection.getSelectionEnd(e))
    }

    @Test
    fun insertsSurrogatePairAndCombiningSequence() {
        val e = editable("")
        "😀".let { Assert.assertEquals(2, insertIntoEditable(e, it)) }
        "री".codePoints().toArray().forEach { insertIntoEditable(e, String(Character.toChars(it))) }
        Assert.assertEquals("😀री", e.toString())
        Assert.assertEquals(e.length, Selection.getSelectionEnd(e))
    }

    @Test
    fun respectsInputFilters() {
        val e = editable("ab", 1)
        e.filters = arrayOf(InputFilter.LengthFilter(2))
        Assert.assertEquals(0, insertIntoEditable(e, "Ж"))
        Assert.assertEquals("ab", e.toString())
        Assert.assertEquals(1, Selection.getSelectionEnd(e))
    }

    @Test
    fun rejectsUnknownSpecialKey() {
        val androidKeyEvent = AndroidKeyEvent(AndroidKeyEventTest.MockUiController()) { null }
        Assert.assertThrows(InvalidArgumentException::class.java) { androidKeyEvent.keyDown(keyEvent("")) }
    }

    @Test
    fun rejectsModifiedNonAsciiCharacter() {
        val androidKeyEvent = AndroidKeyEvent(AndroidKeyEventTest.MockUiController()) { null }
        val event = keyEvent("Ж").also { it.isCtrlKey = true }
        Assert.assertThrows(InvalidArgumentException::class.java) { androidKeyEvent.keyDown(event) }
    }

    @Test
    fun keyUpOfNeverTypedNonAsciiIsNoop() {
        AndroidKeyEvent(AndroidKeyEventTest.MockUiController()) { null }.keyUp(keyEvent("Ж"))
    }

    @Test
    fun releasesPressedKeys() {
        val uiController = AndroidKeyEventTest.MockUiController()
        val androidKeyEvent = AndroidKeyEvent(uiController) { null }
        val shift = W3CKeyEvent().also { it.key = "\uE008"; it.keyCode = KeyEvent.KEYCODE_SHIFT_LEFT }
        androidKeyEvent.keyDown(shift)
        androidKeyEvent.releasePressedKeys()
        val last = uiController.keyEvents.last()
        Assert.assertEquals(KeyEvent.ACTION_UP, last.action)
        Assert.assertEquals(KeyEvent.KEYCODE_SHIFT_LEFT, last.keyCode)
        val count = uiController.keyEvents.size
        androidKeyEvent.releasePressedKeys()
        Assert.assertEquals(count, uiController.keyEvents.size)
    }
}
