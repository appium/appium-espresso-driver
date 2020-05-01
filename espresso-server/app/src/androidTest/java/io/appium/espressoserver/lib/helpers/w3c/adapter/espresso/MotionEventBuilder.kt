package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.PointerProperties
import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException
import io.appium.espressoserver.lib.helpers.AndroidLogger.Companion.logger
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource

class MotionEventBuilder {
    private val motionEventParams: MotionEventParams
    fun withX(x: List<Long>?): MotionEventBuilder {
        motionEventParams.x = x
        return this
    }

    fun withY(y: List<Long>?): MotionEventBuilder {
        motionEventParams.y = y
        return this
    }

    fun withX(x: Long): MotionEventBuilder {
        motionEventParams.x = listOf(x)
        return this
    }

    fun withY(y: Long): MotionEventBuilder {
        motionEventParams.y = listOf(y)
        return this
    }

    fun withDownTime(downTime: Long): MotionEventBuilder {
        motionEventParams.downTime = downTime
        return this
    }

    fun withEventTime(eventTime: Long): MotionEventBuilder {
        motionEventParams.eventTime = eventTime
        return this
    }

    fun withAction(action: Int): MotionEventBuilder {
        motionEventParams.action = action
        return this
    }

    fun withMetaState(metaState: Int): MotionEventBuilder {
        motionEventParams.metaState = metaState
        return this
    }

    fun withButtonState(buttonState: Int): MotionEventBuilder {
        motionEventParams.buttonState = buttonState
        return this
    }

    fun withXPrecision(xPrecision: Float): MotionEventBuilder {
        motionEventParams.xPrecision = xPrecision
        return this
    }

    fun withYPrecision(yPrecision: Float): MotionEventBuilder {
        motionEventParams.yPrecision = yPrecision
        return this
    }

    fun withDeviceId(deviceId: Int): MotionEventBuilder {
        motionEventParams.deviceId = deviceId
        return this
    }

    fun withSource(source: Int): MotionEventBuilder {
        motionEventParams.source = source
        return this
    }

    fun withEdgeFlags(edgeFlags: Int): MotionEventBuilder {
        motionEventParams.edgeFlags = edgeFlags
        return this
    }

    fun withPointerType(pointerType: InputSource.PointerType?): MotionEventBuilder {
        motionEventParams.pointerType = pointerType
        return this
    }

    fun build(): MotionEventRunner {
        return MotionEventRunner(motionEventParams)
    }

    class MotionEventParams {
        var downTime: Long = 0
        var action = 0
        var x: List<Long>? = null
        var y: List<Long>? = null
        var metaState = 0
        var xPrecision = 0f
        var yPrecision = 0f
        var deviceId = 0
        var edgeFlags = 0
        var buttonState = 0
        var source = 0
        var pointerType: InputSource.PointerType? = null
        var eventTime: Long = 0
    }

    class MotionEventRunner(private val motionEventParams: MotionEventParams) {
        @Throws(AppiumException::class)
        fun run(uiController: UiController): MotionEvent? {
            var pointerCount = if (motionEventParams.x == null) 0 else motionEventParams.x!!.size

            // Don't do anything if no pointers were provided
            if (pointerCount == 0 && motionEventParams.action != MotionEvent.ACTION_CANCEL) {
                return null
            }
            val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(pointerCount)
            val pointerProperties = arrayOfNulls<PointerProperties>(pointerCount)
            for (pointerIndex in 0 until pointerCount) {
                // Set pointer coordinates
                pointerCoords[pointerIndex] = MotionEvent.PointerCoords()
                pointerCoords[pointerIndex]!!.clear()
                pointerCoords[pointerIndex]!!.pressure = 1f
                pointerCoords[pointerIndex]!!.size = 1f
                pointerCoords[pointerIndex]!!.x = motionEventParams.x!![pointerIndex].toFloat()
                pointerCoords[pointerIndex]!!.y = motionEventParams.y!![pointerIndex].toFloat()

                // Set pointer properties
                pointerProperties[pointerIndex] = PointerProperties()
                pointerProperties[pointerIndex]!!.toolType = getToolType(motionEventParams.pointerType)
                pointerProperties[pointerIndex]!!.id = pointerIndex
            }

            // ACTION_POINTER_DOWN and ACTION_POINTER_UP need a bit mask
            var action = motionEventParams.action
            if (pointerCount > 1 && (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP)) {
                action += pointerProperties[1]!!.id shl MotionEvent.ACTION_POINTER_INDEX_SHIFT
            }

            // ACTION_DOWN and ACTION_UP and ACTION_CANCEL has a pointer count of 1
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                pointerCount = if (motionEventParams.x != null && motionEventParams.y != null) {
                    1
                } else {
                    0
                }
            }
            val eventTime = if (motionEventParams.eventTime > 0) motionEventParams.eventTime else SystemClock.uptimeMillis()
            logger.info(String.format(
                    "Running Android MotionEvent.obtain with parameters: " +
                            "downTime=[%s], eventTime=[%s], action=[%s], pointerCount=[%s], " + "" +
                            "pointerProperties=[%s], pointerCoords=[%s], metaState=[%s], buttonState=[%s], " +
                            "xPrecision=[%s], yPrecision=[%s], deviceId=[%s], edgeFlags=[%s], source=[%s], " +
                            "flags=[%s]",
                    "For more information, see https://developer.android.com/reference/android/view/MotionEvent#obtain(long,%20long,%20int,%20int,%20android.view.MotionEvent.PointerProperties[],%20android.view.MotionEvent.PointerCoords[],%20int,%20int,%20float,%20float,%20int,%20int,%20int,%20int)",
                    motionEventParams.downTime,
                    eventTime,
                    action,
                    pointerCount,
                    pointerProperties.contentToString(),
                    pointerCoords.contentToString(),
                    motionEventParams.metaState,
                    motionEventParams.buttonState,
                    motionEventParams.xPrecision,
                    motionEventParams.yPrecision,
                    motionEventParams.deviceId,
                    motionEventParams.edgeFlags,
                    motionEventParams.source,
                    0
            ))
            val evt = MotionEvent.obtain(
                    motionEventParams.downTime,
                    eventTime,
                    action,
                    pointerCount,
                    pointerProperties,
                    pointerCoords,
                    motionEventParams.metaState,
                    motionEventParams.buttonState,
                    motionEventParams.xPrecision,
                    motionEventParams.yPrecision,
                    motionEventParams.deviceId,
                    motionEventParams.edgeFlags,
                    motionEventParams.source,
                    0 // MotionEvent flags. Don't think we need to set these.
            )
            try {
                val success = uiController.injectMotionEvent(evt)
                if (!success) {
                    throw AppiumException("Could not complete pointer operation")
                }
            } catch (e: InjectEventSecurityException) {
                throw MoveTargetOutOfBoundsException(String.format(
                        "Could not complete pointer operation. Pointer operation is within the viewport but is not within bounds of the app-under-test. Cause: %s",
                        e.cause
                ))
            }
            return evt
        }

    }

    init {
        motionEventParams = MotionEventParams()
    }
}