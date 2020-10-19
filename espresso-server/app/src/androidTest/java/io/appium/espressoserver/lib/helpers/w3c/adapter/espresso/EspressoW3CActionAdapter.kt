package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.content.Context
import android.graphics.Point

import androidx.test.espresso.UiController
import androidx.test.espresso.action.GeneralLocation
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.Logger
import io.appium.espressoserver.lib.helpers.w3c.adapter.BaseW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.model.Element

import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class EspressoW3CActionAdapter(private val uiController: UiController) : BaseW3CActionAdapter() {

    private val androidKeyEvent: AndroidKeyEvent = AndroidKeyEvent(uiController)
    private val multiTouchState = MultiTouchState()
    private val displayMetrics = getApplicationContext<Context>().resources.displayMetrics

    override val viewportHeight: Long
        get() = displayMetrics.heightPixels.toLong()

    override val viewportWidth: Long
        get() = displayMetrics.widthPixels.toLong()

    override val logger: Logger
        get() = AndroidLogger

    @Throws(AppiumException::class)
    override fun keyDown(keyDownEvent: W3CKeyEvent) {
        androidKeyEvent.keyDown(keyDownEvent)
    }

    @Throws(AppiumException::class)
    override fun keyUp(keyUpEvent: W3CKeyEvent) {
        androidKeyEvent.keyUp(keyUpEvent)
    }

    @Throws(AppiumException::class)
    override fun pointerDown(button: Int, sourceId: String, pointerType: PointerType?,
                    x: Float, y: Float, depressedButtons: Set<Int>,
                    globalKeyInputState: KeyInputState?) {
        this.logger.info("Running pointer down at coordinates: ${x}, ${y}, $pointerType")
        val roundedCoords = toCoordinates(x, y)

        if (isTouch(pointerType)) {
            // touch down actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_DOWN, sourceId, roundedCoords.x.toLong(), roundedCoords.y.toLong(), globalKeyInputState, button)
        } else {
            val androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController)
            val xList = listOf(roundedCoords.x.toLong())
            val yList = listOf(roundedCoords.y.toLong())
            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_DOWN, button, pointerType, globalKeyInputState, null, 0)

            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_POINTER_DOWN, button, pointerType, globalKeyInputState, null, 0)
        }
    }

    @Throws(AppiumException::class)
    override fun pointerUp(button: Int, sourceId: String, pointerType: PointerType?,
                  x: Float, y: Float, depressedButtons: Set<Int>,
                  globalKeyInputState: KeyInputState?) {
        this.logger.info("Running pointer up at coordinates: $x $y $pointerType")
        val roundedCoords = toCoordinates(x, y)
        if (isTouch(pointerType)) {
            // touch up actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_UP, sourceId, roundedCoords.x.toLong(), roundedCoords.y.toLong(), globalKeyInputState, button)
        } else {
            val xList = listOf(roundedCoords.x.toLong())
            val yList = listOf(roundedCoords.y.toLong())
            val androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController)
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_POINTER_UP, button, pointerType, globalKeyInputState, null, 0)
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_UP, button, pointerType, globalKeyInputState, null, 0)
        }
    }

    @Throws(AppiumException::class)
    override fun pointerMove(sourceId: String, pointerType: PointerType?,
                             currentX: Float, currentY: Float, x: Float, y: Float,
                             buttons: Set<Int>?, globalKeyInputState: KeyInputState?) {
        this.logger.info("Running pointer move at coordinates: $x $y $pointerType")
        val roundedCoords = toCoordinates(x, y)
        if (isTouch(pointerType)) {
            multiTouchState.updateTouchState(ACTION_MOVE, sourceId, roundedCoords.x.toLong(), roundedCoords.y.toLong(), globalKeyInputState, null)
            multiTouchState.pointerMove(uiController)
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                    .pointerMove(listOf(roundedCoords.x.toLong()), listOf(roundedCoords.y.toLong()), pointerType, globalKeyInputState, null)
        }
    }

    @Throws(AppiumException::class)
    override fun pointerCancel(sourceId: String, pointerType: PointerType) {
        if (isTouch(pointerType)) {
            multiTouchState.pointerCancel(uiController)
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController).pointerCancel()
        }
    }

    @Throws(AppiumException::class)
    override fun sychronousTickActionsComplete() {
        multiTouchState.perform(uiController)
        AndroidLogger.info("Pointer event: Tick complete")
    }

    override fun getKeyCode(keyValue: String?, location: Int): Int {
        return keyCodeToEvent(keyValue, location)
    }

    @Throws(AppiumException::class)
    override fun getElementCenterPoint(elementId: String?): Point {
        val view = Element.getViewById(elementId)
        val coords = GeneralLocation.CENTER.calculateCoordinates(view)
        val point = Point()
        point.x = coords[0].roundToInt()
        point.y = coords[1].roundToInt()
        return point
    }

    override fun waitForUiThread() {
        uiController.loopMainThreadUntilIdle()
    }

    override fun sleep(duration: Float) {
        val roundedDuration = duration.roundToLong()
        uiController.loopMainThreadForAtLeast(roundedDuration)
    }
}
