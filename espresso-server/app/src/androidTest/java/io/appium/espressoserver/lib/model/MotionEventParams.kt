package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

import android.view.MotionEvent.BUTTON_PRIMARY
import android.view.MotionEvent.BUTTON_SECONDARY
import android.view.MotionEvent.BUTTON_TERTIARY

class MotionEventParams : AppiumParams() {

    @SerializedName("element")
    override var elementId: String? = null
    var x: Long? = null
    var y: Long? = null
    private var button: Int? = null


    val androidButtonState: Int
        @Throws(InvalidArgumentException::class)
        get() = getAndroidButtonState(this.getButton())

    fun getButton(): Int? {
        // Left button is the default
        return if (button == null) {
            MOUSE_LEFT
        } else button
    }

    fun setButton(button: Int) {
        this.button = button
    }

    companion object {


        val MOUSE_LEFT = 0
        val MOUSE_MIDDLE = 1
        val MOUSE_RIGHT = 2

        @Throws(InvalidArgumentException::class)
        fun getAndroidButtonState(button: Int?): Int {
            if (button == null) {
                return BUTTON_PRIMARY
            }

            when (button) {
                MOUSE_LEFT -> return BUTTON_PRIMARY
                MOUSE_MIDDLE -> return BUTTON_TERTIARY
                MOUSE_RIGHT -> return BUTTON_SECONDARY
                else -> throw InvalidArgumentException(String.format("'%s' is not a valid button type", button))
            }

        }
    }
}
