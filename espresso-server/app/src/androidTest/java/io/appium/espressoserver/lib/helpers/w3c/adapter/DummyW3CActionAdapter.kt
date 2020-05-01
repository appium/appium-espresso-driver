package io.appium.espressoserver.lib.helpers.w3c.adapter

import android.graphics.Point

import java.util.ArrayList

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.helpers.Logger
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

open class DummyW3CActionAdapter : BaseW3CActionAdapter() {

    // Keep a log of pointer move events so the values can be checked in the unit tests
    private val pointerMoveEvents = ArrayList<PointerMoveEvent>()
    override val logger: Logger = DummyLogger()

    override val viewportHeight: Long
        get() = 400

    override val viewportWidth: Long
        get() = 200

    class PointerMoveEvent {
        var sourceId: String? = null
        var pointerType: PointerType? = null
        var currentX: Float = 0.toFloat()
        var currentY: Float = 0.toFloat()
        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()
        var buttons: Set<Int>? = null
        var globalKeyInputState: KeyInputState? = null
    }

    private inner class DummyLogger : Logger {
        override fun error(vararg messages: Any) {
            // No-op
        }

        override fun error(message: String, throwable: Throwable) {
            // No-op
        }

        override fun info(vararg messages: Any) {
            // No-op
        }

        override fun debug(vararg messages: Any) {
            // No-op
        }

        override fun warn(vararg messages: Any) {
            // No-op
        }
    }

    override fun keyDown(keyDownEvent: W3CKeyEvent) {
        // No-op
    }

    override fun keyUp(keyUpEvent: W3CKeyEvent) {
        // No-op
    }

    @Throws(AppiumException::class)
    override fun pointerUp(button: Int, sourceId: String, pointerType: PointerType?,
                       x: Float, y: Float, depressedButtons: Set<Int>,
                       globalKeyInputState: KeyInputState?) {
        // No-op
    }

    @Throws(AppiumException::class)
    override fun pointerDown(button: Int, sourceId: String, pointerType: PointerType?,
                         x: Float, y: Float, depressedButtons: Set<Int>,
                         globalKeyInputState: KeyInputState?) {
        // No-op
    }

    override fun pointerCancel(sourceId: String, pointerType: PointerType) {
        // No-op
    }

    override fun getPointerMoveDurationMargin(pointerInputState: PointerInputState): Double {
        return if (pointerInputState.type == PointerType.TOUCH && !pointerInputState.hasPressedButtons()) {
            // If no buttons are pushed nothing happens, so skip to the end
            // of the pointer move
            // e.g.: touch move without pressed buttons is like a finger moving without
            //      being pressed on the screen
            1.0
        } else 0.01

        // Give a margin of error of 1%
    }

    override fun pointerMove(sourceId: String, pointerType: PointerType?,
                             currentX: Float, currentY: Float, x: Float, y: Float,
                             buttons: Set<Int>?, globalKeyInputState: KeyInputState?) {
        // Add the pointer move event to the logs
        val pointerMoveEvent = PointerMoveEvent()
        pointerMoveEvent.apply {
            this.sourceId = sourceId
            this.pointerType = pointerType
            this.currentX = currentX
            this.currentY = currentY
            this.x = x
            this.y = y
            this.buttons = buttons
            this.globalKeyInputState = globalKeyInputState
        }
        pointerMoveEvents.add(pointerMoveEvent)
    }

    fun getPointerMoveEvents(): List<PointerMoveEvent> {
        return pointerMoveEvents
    }

    @Throws(AppiumException::class)
    override fun getElementCenterPoint(elementId: String?): Point {
        when(elementId) {
            "none" -> throw NoSuchElementException("Could not find element with id: ${elementId}")
            "stale" -> throw StaleElementException("Element with id ${elementId} no longer exists")
        }

        val point = Point()
        point.x = 10
        point.y = 10
        return point
    }
}
