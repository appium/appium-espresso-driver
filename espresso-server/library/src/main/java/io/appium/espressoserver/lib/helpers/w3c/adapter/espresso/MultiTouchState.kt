package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.AndroidMotionEvent.Companion.getTouchMotionEvent
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import java.util.*

class MultiTouchState {
    private val touchStateSet: MutableMap<String, TouchState> = LinkedHashMap()
    private var globalKeyInputState: KeyInputState? = null
    private var button = 0
    private var downEvent: MotionEvent? = null
    private var touchPhase: TouchPhase? = null
    fun updateTouchState(actionType: Int,
                         sourceId: String,
                         x: Long?, y: Long?,
                         globalKeyInputState: KeyInputState?,
                         button: Int?) {

        // Lazily get the touch state of the input with given sourceId
        if (!touchStateSet.containsKey(sourceId)) {
            val touchState = TouchState()
            touchStateSet[sourceId] = touchState
        }

        // Update x and y coordinates
        val touchState = touchStateSet[sourceId]!!
        touchState.x = x!!
        touchState.y = y!!

        // Update to global key input state
        this.globalKeyInputState = globalKeyInputState
        if (button != null) {
            this.button = button
        }

        // Record if we're in the TOUCH_DOWN or TOUCH_UP phase
        if (actionType == MotionEvent.ACTION_DOWN) {
            touchPhase = TouchPhase.DOWN
        } else if (actionType == MotionEvent.ACTION_UP) {
            touchPhase = TouchPhase.UP
        }
    }

    /**
     * Get the x coordinates for all inputs in the same order they were entered
     * @return X coordinates as a list
     */
    private val xCoords: List<Long>
        get() { return touchStateSet.values.map { it.x } }

    /**
     * Get the y coordinates for all inputs in the same order they were entered
     * @return Y coordinates as a list
     */
    private val yCoords: List<Long>
        get() { return touchStateSet.values.map { it.y } }

    @Throws(AppiumException::class)
    fun pointerDown(uiController: UiController) {
        val androidMotionEvent = getTouchMotionEvent(uiController)
        val eventTime = SystemClock.uptimeMillis()
        val xCoords = xCoords
        val yCoords = yCoords
        downEvent = androidMotionEvent.pointerEvent(
                xCoords, yCoords,
                MotionEvent.ACTION_DOWN, button, InputSource.PointerType.TOUCH, globalKeyInputState!!, null, eventTime)
        if (xCoords.size > 1) {
            androidMotionEvent.pointerEvent(xCoords, yCoords,
                    MotionEvent.ACTION_POINTER_DOWN, button, InputSource.PointerType.TOUCH, globalKeyInputState!!, downEvent, eventTime)
        }
    }

    @Throws(AppiumException::class)
    fun pointerUp(uiController: UiController) {
        val androidMotionEvent = getTouchMotionEvent(uiController)
        val eventTime = SystemClock.uptimeMillis()
        val xCoords = xCoords
        val yCoords = yCoords
        if (xCoords.size > 1) {
            androidMotionEvent.pointerEvent(
                    xCoords, yCoords,
                    MotionEvent.ACTION_POINTER_UP, button, InputSource.PointerType.TOUCH, globalKeyInputState!!, downEvent, eventTime)
        }
        androidMotionEvent.pointerEvent(
                xCoords, yCoords,
                MotionEvent.ACTION_UP, button, InputSource.PointerType.TOUCH, globalKeyInputState!!, downEvent, eventTime)
        downEvent = null
    }

    @Throws(AppiumException::class)
    fun pointerCancel(uiController: UiController) {
        val xCoords = xCoords
        val yCoords = yCoords
        getTouchMotionEvent(uiController).pointerEvent(
                xCoords, yCoords,
                MotionEvent.ACTION_CANCEL, button, InputSource.PointerType.TOUCH, globalKeyInputState!!, downEvent, SystemClock.uptimeMillis()
        )
        downEvent = null
        touchPhase = TouchPhase.NONE
    }

    @Throws(AppiumException::class)
    fun pointerMove(uiController: UiController) {
        if (isDown) {
            val androidMotionEvent = getTouchMotionEvent(uiController)
            androidMotionEvent.pointerMove(
                    xCoords, yCoords, InputSource.PointerType.TOUCH, globalKeyInputState!!, downEvent)
        }
    }

    val isDown: Boolean
        get() = downEvent != null

    @Throws(AppiumException::class)
    fun perform(uiController: UiController) {
        if (touchPhase == TouchPhase.DOWN) {
            pointerDown(uiController)
        } else if (touchPhase == TouchPhase.UP) {
            pointerUp(uiController)
        }
        touchPhase = TouchPhase.NONE
    }

    enum class TouchPhase {
        DOWN, UP, NONE
    }
}