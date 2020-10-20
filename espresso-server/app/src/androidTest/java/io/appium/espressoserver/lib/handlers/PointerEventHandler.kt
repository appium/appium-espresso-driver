/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.handlers

import android.content.res.Resources
import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.ViewConfiguration
import androidx.test.espresso.UiController
import androidx.test.espresso.action.ViewActions.*
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MotionEventBuilder
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.MOUSE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.MotionEventParams
import io.appium.espressoserver.lib.model.ViewElement
import io.appium.espressoserver.lib.model.toAndroidButtonState
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable
import java.util.*

class PointerEventHandler(private val touchType: TouchType) : RequestHandler<MotionEventParams, Void?> {

    // Make the duration of the TAP and DOUBLE_TAP events less than the timeout. So make the number
    // half of the timeouts.
    private val TAP_DURATION = (ViewConfiguration.getTapTimeout() / 2).toLong()
    private val DOUBLE_TAP_DURATION = (ViewConfiguration.getDoubleTapTimeout() / 2).toLong()


    private val globalButtonState: Int
        @Throws(InvalidArgumentException::class)
        get() {
            return globalMouseButtonDownEvents.keys.fold(0) { buttonState, button ->
                buttonState or button.toAndroidButtonState()
            }
        }

    enum class TouchType {
        CLICK,
        DOUBLE_CLICK,
        LONG_CLICK,
        SCROLL,
        TOUCH_DOWN,
        TOUCH_UP,
        TOUCH_MOVE,
        TOUCH_SCROLL,
        MOUSE_UP,
        MOUSE_DOWN,
        MOUSE_MOVE,
        MOUSE_CLICK,
        MOUSE_DOUBLECLICK
    }

    @Throws(AppiumException::class)
    override fun handleInternal(params: MotionEventParams): Void? {
        when (touchType) {
            TouchType.CLICK -> handleClick(params)
            TouchType.DOUBLE_CLICK -> handleDoubleClick(params)
            TouchType.LONG_CLICK -> handleLongClick(params)
            TouchType.TOUCH_DOWN -> handleTouchDown(params)
            TouchType.TOUCH_UP -> handleTouchUp(params)
            TouchType.TOUCH_MOVE -> handleTouchMove(params)
            TouchType.TOUCH_SCROLL -> handleTouchScroll(params)
            TouchType.MOUSE_DOWN -> handleMouseButtonDown(params)
            TouchType.MOUSE_UP -> handleMouseButtonUp(params)
            TouchType.MOUSE_MOVE -> handleMouseMove(params)
            TouchType.MOUSE_CLICK -> handleMouseClick(params)
            TouchType.MOUSE_DOUBLECLICK -> handleMouseDoubleClick(params)
            TouchType.SCROLL -> handleTouchScroll(params)
        }
        return null
    }


    @Throws(AppiumException::class)
    private fun handleTouchDown(params: MotionEventParams) {
        globalTouchDownEvent?.let {
            throw AppiumException("Cannot call touch down while another touch event is still down")
        }
        AndroidLogger.info("Calling touch down event on (${params.x} ${params.y})")
        globalTouchDownEvent = handlePointerEvent(params, ACTION_DOWN, TOUCH)
    }

    @Throws(AppiumException::class)
    private fun handleTouchUp(params: MotionEventParams) {
        AndroidLogger.info("Calling touch up event on (${params.x} ${params.y})")
        globalTouchDownEvent
                ?: throw AppiumException("Touch up event must be preceded by a touch down event")
        handlePointerEvent(params, ACTION_UP, TOUCH, globalTouchDownEvent!!.downTime)
        globalTouchDownEvent = null
    }

    @Throws(AppiumException::class)
    private fun handleTouchMove(params: MotionEventParams) {
        globalTouchDownEvent
                ?: throw AppiumException("Touch move event must have a touch down event")
        handlePointerEvent(params, ACTION_MOVE, TOUCH, globalTouchDownEvent!!.downTime)
    }

    @Throws(AppiumException::class)
    private fun handleTouchScroll(params: MotionEventParams) {
        // Fabricate a scroll event
        // Do halfway points, by default
        var startX = displayMetrics.widthPixels / 2 - params.x / 2
        var startY = displayMetrics.heightPixels / 2 - params.y / 2

        params.targetElement?.let {
            val view = Element.getViewById(it)
            val viewElement = ViewElement(view)
            startX = viewElement.bounds.left.toLong()
            startY = viewElement.bounds.top.toLong()
        }

        // Do down event
        val downParams = MotionEventParams(startX, startY)
        val downEvent = handlePointerEvent(downParams, ACTION_DOWN, TOUCH)

        val downTime = downEvent.downTime
        var eventTime: Long = downTime

        // For it to be considered a 'scroll', must hold down for longer then tap timeout duration
        val scrollDuration = (ViewConfiguration.getTapTimeout() * 1.5).toLong()

        eventTime += scrollDuration
        val moveParams = MotionEventParams(params.x + startX, params.y + startY)
        handlePointerEvent(moveParams, ACTION_MOVE, TOUCH, downTime, eventTime)

        // Release finger after another 'scroll' duration
        eventTime += scrollDuration
        val upParams = MotionEventParams(startX + params.x, startY + params.y)
        handlePointerEvent(upParams, ACTION_UP, TOUCH, downTime, eventTime)
    }

    @Throws(AppiumException::class)
    private fun handleMouseButtonDown(params: MotionEventParams) {
        val mouseDownEvent = handlePointerEvent(params, ACTION_DOWN, MOUSE)
        globalMouseButtonDownEvents[params.button] = mouseDownEvent
        handlePointerEvent(params, ACTION_DOWN, MOUSE, SystemClock.uptimeMillis())
    }

    @Throws(AppiumException::class)
    private fun handleMouseButtonUp(params: MotionEventParams) {
        val mouseDownEvent = globalMouseButtonDownEvents[params.button]
                ?: throw AppiumException(String.format(
                        "Mouse button up event '%s' must be preceded by a mouse down event",
                        params.button
                ))
        handlePointerEvent(params, ACTION_UP, MOUSE, mouseDownEvent.downTime)
        globalMouseButtonDownEvents.remove(params.button)
    }

    @Throws(AppiumException::class)
    private fun handleMouseMove(params: MotionEventParams) {
        params.button = globalButtonState
        handlePointerEvent(params, ACTION_MOVE, MOUSE)
        globalMouseLocationX = params.x
        globalMouseLocationY = params.y
    }

    @Throws(AppiumException::class)
    private fun handleClick(params: MotionEventParams) {
        params.targetElement
                ?: throw InvalidArgumentException("Element ID must not be blank for click event")
        Element.getViewInteractionById(params.targetElement).perform(click())
    }

    @Throws(AppiumException::class)
    private fun handleDoubleClick(params: MotionEventParams) {
        params.targetElement
                ?: throw InvalidArgumentException("Element ID must not be blank for double click event")
        Element.getViewInteractionById(params.targetElement).perform(doubleClick())
    }

    @Throws(AppiumException::class)
    private fun handleLongClick(params: MotionEventParams) {
        params.targetElement
                ?: throw InvalidArgumentException("Element ID must not be blank for long click event")
        Element.getViewInteractionById(params.targetElement).perform(longClick())
    }

    @Throws(AppiumException::class)
    private fun handleMouseDoubleClick(params: MotionEventParams) {
        params.x = globalMouseLocationX
        params.y = globalMouseLocationY
        var eventTime = SystemClock.uptimeMillis()
        for (clickNumber in 1..2) {
            val downTime = eventTime
            handlePointerEvent(params, ACTION_DOWN, MOUSE, downTime, eventTime)
            eventTime += TAP_DURATION
            handlePointerEvent(params, ACTION_UP, MOUSE, downTime, eventTime)
            eventTime += DOUBLE_TAP_DURATION
        }
    }

    @Throws(AppiumException::class)
    private fun handleMouseClick(params: MotionEventParams) {
        params.x = globalMouseLocationX
        params.y = globalMouseLocationY
        val downTime = SystemClock.uptimeMillis()
        handlePointerEvent(params, ACTION_DOWN, MOUSE, downTime, downTime)
        handlePointerEvent(params, ACTION_UP, MOUSE, downTime, downTime + TAP_DURATION)
    }

    companion object {
        private var globalTouchDownEvent: MotionEvent? = null
        private val globalMouseButtonDownEvents = HashMap<Int, MotionEvent>()
        private var globalMouseLocationX: Long = 0L
        private var globalMouseLocationY: Long = 0L
        private val displayMetrics = Resources.getSystem().displayMetrics

        @Throws(AppiumException::class)
        private fun checkBounds(x: Long, y: Long) {
            if (x < 0 || y < 0 || x > displayMetrics.widthPixels || y > displayMetrics.heightPixels) {
                throw AppiumException(String.format("Coordinates [%s %s] are outside of viewport [%s %s]",
                        x, y, displayMetrics.widthPixels, displayMetrics.heightPixels))
            }
        }

        @Synchronized
        @Throws(AppiumException::class)
        private fun handlePointerEvent(params: MotionEventParams,
                                       action: Int,
                                       pointerType: PointerType,
                                       downTime: Long?,
                                       eventTime: Long?): MotionEvent {
            checkBounds(params.x, params.y)
            val runnable = object : UiControllerRunnable<MotionEvent> {
                override fun run(uiController: UiController): MotionEvent {
                    return MotionEventBuilder()
                            .withDownTime(downTime ?: SystemClock.uptimeMillis())
                            .withEventTime(eventTime ?: SystemClock.uptimeMillis())
                            .withX(params.x)
                            .withY(params.y)
                            .withPointerType(pointerType)
                            .withButtonState(params.androidButtonState)
                            .withAction(action)
                            .build()
                            .run(uiController)!!
                }
            }

            return UiControllerPerformer(runnable).run()!!
        }

        @Synchronized
        @Throws(AppiumException::class)
        private fun handlePointerEvent(params: MotionEventParams,
                                       action: Int,
                                       pointerType: PointerType,
                                       downTime: Long?): MotionEvent {
            return handlePointerEvent(params, action, pointerType, downTime, SystemClock.uptimeMillis())
        }

        @Synchronized
        @Throws(AppiumException::class)
        private fun handlePointerEvent(params: MotionEventParams,
                                       action: Int,
                                       pointerType: PointerType): MotionEvent {
            return handlePointerEvent(params, action, pointerType, SystemClock.uptimeMillis(), SystemClock.uptimeMillis())
        }
    }
}
