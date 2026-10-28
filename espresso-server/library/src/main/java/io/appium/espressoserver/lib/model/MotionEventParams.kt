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
    val targetElement: String? = null,
    @SerializedName("x")
    private var _x: Long? = null,
    @SerializedName("y")
    private var _y: Long? = null,
    @SerializedName("button")
    private var _button: Int? = null
) : AppiumParams() {

    constructor(x: Long, y: Long) : this(null, x, y)

    var x: Long
        get() = _x ?: 0L
        set(value) {
            _x = value
        }

    var y: Long
        get() = _y ?: 0L
        set(value) {
            _y = value
        }

    var button: Int
        get() = _button ?: MOUSE_LEFT
        set(value) {
            _button = value
        }

    val androidButtonState: Int
        @Throws(InvalidArgumentException::class)
        get() = this.button.toAndroidButtonState()
}
