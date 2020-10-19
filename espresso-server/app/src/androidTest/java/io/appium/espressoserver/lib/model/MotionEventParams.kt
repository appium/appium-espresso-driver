package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException

import android.view.MotionEvent.BUTTON_PRIMARY
import android.view.MotionEvent.BUTTON_SECONDARY
import android.view.MotionEvent.BUTTON_TERTIARY

const val MOUSE_LEFT = 0
const val MOUSE_MIDDLE = 1
const val MOUSE_RIGHT = 2

fun Int.toAndroidButtonState(): Int {
    return when (this) {
        MOUSE_LEFT -> BUTTON_PRIMARY
        MOUSE_MIDDLE -> BUTTON_TERTIARY
        MOUSE_RIGHT -> BUTTON_SECONDARY
        else -> throw InvalidArgumentException(String.format("'%s' is not a valid button type", this))
    }
}

data class MotionEventParams(
    @SerializedName("element", alternate = [W3C_ELEMENT_KEY])
    val targetElement: String?,
    var x: Long = 0,
    var y: Long = 0,
    var button: Int = MOUSE_LEFT
) : AppiumParams() {

    constructor(x: Long, y: Long) : this(null, x, y)

    val androidButtonState: Int
        @Throws(InvalidArgumentException::class)
        get() = this.button.toAndroidButtonState()
}
