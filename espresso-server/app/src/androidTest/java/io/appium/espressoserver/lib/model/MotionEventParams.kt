package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

import android.view.MotionEvent.BUTTON_PRIMARY
import android.view.MotionEvent.BUTTON_SECONDARY
import android.view.MotionEvent.BUTTON_TERTIARY
import com.google.gson.annotations.SerializedName

data class MotionEventParams(var x: Long, var y: Long) : AppiumParams() {

    @SerializedName("element")
    var targetElement: String? = null
    var button: Int = MOUSE_LEFT

    val androidButtonState: Int
        @Throws(InvalidArgumentException::class)
        get() = getAndroidButtonState(this.button)

    companion object {

        val MOUSE_LEFT = 0
        val MOUSE_MIDDLE = 1
        val MOUSE_RIGHT = 2

        @Throws(InvalidArgumentException::class)
        fun getAndroidButtonState(button: Int): Int {
            when (button) {
                MOUSE_LEFT -> return BUTTON_PRIMARY
                MOUSE_MIDDLE -> return BUTTON_TERTIARY
                MOUSE_RIGHT -> return BUTTON_SECONDARY
                else -> throw InvalidArgumentException(String.format("'%s' is not a valid button type", button))
            }

        }
    }
}
