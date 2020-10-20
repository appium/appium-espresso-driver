package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.PointerProperties
import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource

class MotionEventBuilder {
    private val motionEventParams: MotionEventParams
    fun withX(x: List<Long>?): MotionEventBuilder = apply { motionEventParams.x = x }
    fun withY(y: List<Long>?): MotionEventBuilder = apply { motionEventParams.y = y }
    fun withX(x: Long): MotionEventBuilder = apply { motionEventParams.x = listOf(x) }
    fun withY(y: Long): MotionEventBuilder = apply { motionEventParams.y = listOf(y) }
    fun withDownTime(downTime: Long): MotionEventBuilder = apply { motionEventParams.downTime = downTime }
    fun withEventTime(eventTime: Long): MotionEventBuilder = apply { motionEventParams.eventTime = eventTime }
    fun withAction(action: Int): MotionEventBuilder = apply { motionEventParams.action = action }
    fun withMetaState(metaState: Int): MotionEventBuilder = apply { motionEventParams.metaState = metaState }
    fun withButtonState(buttonState: Int): MotionEventBuilder = apply { motionEventParams.buttonState = buttonState }
    fun withXPrecision(xPrecision: Float): MotionEventBuilder = apply { motionEventParams.xPrecision = xPrecision }
    fun withYPrecision(yPrecision: Float): MotionEventBuilder = apply { motionEventParams.yPrecision = yPrecision }
    fun withDeviceId(deviceId: Int): MotionEventBuilder = apply { motionEventParams.deviceId = deviceId }
    fun withSource(source: Int): MotionEventBuilder = apply { motionEventParams.source = source }
    fun withEdgeFlags(edgeFlags: Int): MotionEventBuilder = apply { motionEventParams.edgeFlags = edgeFlags }
    fun withPointerType(pointerType: InputSource.PointerType?): MotionEventBuilder = apply { motionEventParams.pointerType = pointerType }

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
                pointerCoords[pointerIndex]!!.let {
                    it.clear()
                    it.pressure = 1f
                    it.size = 1f
                    it.x = motionEventParams.x!![pointerIndex].toFloat()
                    it.y = motionEventParams.y!![pointerIndex].toFloat()
                }

                // Set pointer properties
                pointerProperties[pointerIndex] = PointerProperties()
                pointerProperties[pointerIndex]!!.let {
                    it.toolType = getToolType(motionEventParams.pointerType)
                    it.id = pointerIndex
                }
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
            AndroidLogger.info(String.format(
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
