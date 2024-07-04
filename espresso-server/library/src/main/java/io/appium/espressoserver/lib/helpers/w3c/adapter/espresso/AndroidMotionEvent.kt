package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.view.MotionEvent
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import java.util.concurrent.ConcurrentHashMap

class AndroidMotionEvent private constructor(private val uiController: UiController) {
    private var downTime: Long = 0

    @Throws(AppiumException::class)
    fun pointerEvent(x: List<Long>?, y: List<Long>?,
                     action: Int,
                     button: Int?,
                     pointerType: InputSource.PointerType?,
                     globalKeyInputState: KeyInputState?,
                     downEvent: MotionEvent?,
                     eventTime: Long): MotionEvent {
        val metaState = globalKeyInputState?.toMetaState() ?: 0
        downTime = downEvent?.downTime ?: eventTime
        return MotionEventBuilder()
                .withAction(action)
                .withButtonState(extractButton(button, pointerType))
                .withPointerType(pointerType)
                .withDownTime(downTime)
                .withEventTime(eventTime)
                .withX(x)
                .withY(y)
                .withMetaState(metaState)
                .withSource(downEvent?.source ?: 0)
                .build()
                .run(uiController)!!
    }

    @Throws(AppiumException::class)
    fun pointerMove(x: List<Long>?, y: List<Long>?,
                    pointerType: InputSource.PointerType?,
                    globalKeyInputState: KeyInputState?,
                    downEvent: MotionEvent?) {
        val metaState = globalKeyInputState?.toMetaState() ?: 0
        MotionEventBuilder()
                .withAction(MotionEvent.ACTION_MOVE)
                .withDownTime(downTime)
                .withPointerType(pointerType)
                .withX(x)
                .withY(y)
                .withMetaState(metaState)
                .withSource(downEvent?.source ?: 0)
                .build()
                .run(uiController)
    }

    @JvmOverloads
    @Throws(AppiumException::class)
    fun pointerCancel(x: List<Long>? = null, y: List<Long>? = null) {
        MotionEventBuilder()
                .withAction(MotionEvent.ACTION_CANCEL)
                .withDownTime(downTime)
                .withX(x)
                .withY(y)
                .withPointerType(InputSource.PointerType.TOUCH)
                .build()
                .run(uiController)
    }

    companion object {
        private var touchMotionEvent: AndroidMotionEvent? = null
        private val motionEvents: MutableMap<String, AndroidMotionEvent> = ConcurrentHashMap()

        @Synchronized
        fun getMotionEvent(
                sourceId: String, uiController: UiController): AndroidMotionEvent {
            if (!motionEvents.containsKey(sourceId)) {
                motionEvents[sourceId] = AndroidMotionEvent(uiController)
            }
            return motionEvents[sourceId]!!
        }

        @Synchronized
        fun getTouchMotionEvent(uiController: UiController): AndroidMotionEvent {
            if (touchMotionEvent == null) {
                touchMotionEvent = AndroidMotionEvent(uiController)
            }
            return touchMotionEvent!!
        }
    }

}