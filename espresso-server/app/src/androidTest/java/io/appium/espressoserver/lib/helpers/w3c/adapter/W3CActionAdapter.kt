package io.appium.espressoserver.lib.helpers.w3c.adapter

import android.graphics.Point

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.Logger
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

interface W3CActionAdapter {

    val viewportWidth: Long

    val viewportHeight: Long

    val logger: Logger

    @Throws(AppiumException::class)
    fun keyDown(keyDownEvent: W3CKeyEvent)

    @Throws(AppiumException::class)
    fun keyUp(keyUpEvent: W3CKeyEvent)

    @Throws(AppiumException::class)
    fun pointerDown(button: Int, sourceId: String, pointerType: PointerType?,
                    x: Float, y: Float, depressedButtons: Set<Int>,
                    globalKeyInputState: KeyInputState?)

    @Throws(AppiumException::class)
    fun pointerUp(button: Int, sourceId: String, pointerType: PointerType?,
                  x: Float, y: Float, depressedButtons: Set<Int>,
                  globalKeyInputState: KeyInputState?)

    @Throws(AppiumException::class)
    fun pointerMove(sourceId: String, pointerType: PointerType?,
                    currentX: Float, currentY: Float, x: Float, y: Float,
                    buttons: Set<Int>?, globalKeyInputState: KeyInputState?)

    @Throws(AppiumException::class)
    fun pointerCancel(sourceId: String, pointerType: PointerType)

    fun lockAdapter()

    fun unlockAdapter()

    @Throws(AppiumException::class)
    fun getKeyCode(keyValue: String?, location: Int): Int

    @Throws(AppiumException::class)
    fun getCharCode(keyValue: String?, location: Int): Int

    @Throws(AppiumException::class)
    fun getWhich(keyValue: String?, location: Int): Int

    fun getPointerMoveDurationMargin(pointerInputState: PointerInputState): Double

    fun pointerMoveIntervalDuration(): Int

    @Throws(AppiumException::class)
    fun sleep(duration: Float)

    fun waitForUiThread()

    @Throws(AppiumException::class)
    fun getElementCenterPoint(elementId: String?): Point

    @Throws(AppiumException::class)
    fun sychronousTickActionsComplete()
}
