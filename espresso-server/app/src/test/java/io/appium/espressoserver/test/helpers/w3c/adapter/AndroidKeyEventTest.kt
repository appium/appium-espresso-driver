package io.appium.espressoserver.test.helpers.w3c.adapter

import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.AndroidKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.keyCodeToEvent
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.BACKSPACE
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.getKeyEvent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AndroidKeyEventTest {
    class MockUiController : UiController {
        var keyEvents: MutableList<KeyEvent> = LinkedList()
        override fun injectMotionEvent(event: MotionEvent): Boolean {
            return false
        }

        override fun injectMotionEventSequence(events: Iterable<MotionEvent>): Boolean {
            return false
        }

        override fun injectKeyEvent(event: KeyEvent): Boolean {
            keyEvents.add(event)
            return true
        }

        override fun injectString(str: String): Boolean {
            return false
        }

        override fun loopMainThreadUntilIdle() {
            SystemClock.sleep(100)
        }

        override fun loopMainThreadForAtLeast(millisDelay: Long) {
            SystemClock.sleep(millisDelay)
        }
    }

    @Test
    @Throws(AppiumException::class)
    fun keyUpNoop() {
        // Dispatch an up event that has no corresponding down event and check that the state was unchanged
        val w3CKeyEvent = W3CKeyEvent()
        w3CKeyEvent.key = "A"
        val mockUiController = MockUiController()
        Assert.assertEquals(mockUiController.keyEvents.size.toLong(), 0)
        AndroidKeyEvent(mockUiController).keyUp(w3CKeyEvent)
        Assert.assertEquals(mockUiController.keyEvents.size.toLong(), 0)
    }

    @Test
    @Throws(AppiumException::class)
    fun keyDown() {
        val mockUiController = MockUiController()

        // Dispatch a key event
        val w3CKeyEvent = getKeyEvent(EspressoW3CActionAdapter(mockUiController), "B")
        w3CKeyEvent.isAltKey = true
        w3CKeyEvent.isShiftKey = true
        w3CKeyEvent.isCtrlKey = false
        AndroidKeyEvent(mockUiController).keyDown(w3CKeyEvent)

        // Check that one was dispatched
        Assert.assertEquals(mockUiController.keyEvents.size.toLong(), 1)

        // Check the right key event was dispatched
        val keyEvent = mockUiController.keyEvents[mockUiController.keyEvents.size - 1]
        keyEvent.keyCode
        Assert.assertEquals(keyEvent.keyCode.toLong(), KeyEvent.KEYCODE_B.toLong())
        Assert.assertEquals(keyEvent.action.toLong(), KeyEvent.ACTION_DOWN.toLong())
        Assert.assertEquals(keyEvent.metaState, KeyEvent.META_SHIFT_MASK or KeyEvent.META_ALT_MASK)
    }

    @Test
    @Throws(AppiumException::class)
    fun keyDownAndUp() {
        val mockUiController = MockUiController()
        val androidKeyEvent = AndroidKeyEvent(mockUiController)

        // Key Down C
        val w3CKeyEvent = getKeyEvent(EspressoW3CActionAdapter(mockUiController), "C")
        androidKeyEvent.keyDown(w3CKeyEvent)

        // Check that KEYCODE_C went down
        var keyEvent = mockUiController.keyEvents[mockUiController.keyEvents.size - 1]
        keyEvent.keyCode
        Assert.assertEquals(keyEvent.keyCode.toLong(), KeyEvent.KEYCODE_C.toLong())
        Assert.assertEquals(keyEvent.action.toLong(), KeyEvent.ACTION_DOWN.toLong())

        // Release Key Code C and check that there's no keys down
        androidKeyEvent.keyUp(w3CKeyEvent)
        keyEvent = mockUiController.keyEvents[mockUiController.keyEvents.size - 1]
        keyEvent.keyCode
        Assert.assertEquals(keyEvent.keyCode.toLong(), KeyEvent.KEYCODE_C.toLong())
        Assert.assertEquals(keyEvent.action.toLong(), KeyEvent.ACTION_UP.toLong())
    }

    @Test
    fun keyCode() {
        Assert.assertEquals(keyCodeToEvent(BACKSPACE, 0).toLong(), KeyEvent.KEYCODE_DEL.toLong())
        Assert.assertEquals(keyCodeToEvent("A", 0).toLong(), -1)
    }
}