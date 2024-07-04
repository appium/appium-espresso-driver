package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso

import android.graphics.Point
import android.os.Build
import android.view.MotionEvent
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import kotlin.math.roundToInt

private const val MOUSE_BUTTON_LEFT = 0
private const val MOUSE_BUTTON_MIDDLE = 1
private const val MOUSE_BUTTON_RIGHT = 2

@Throws(AppiumException::class)
fun extractButton(w3cButton: Int?, pointerType: InputSource.PointerType?): Int {
    // Get the Android tool type constant associated with the W3C provided pointer type
    val toolType = getToolType(pointerType)
    if (toolType == MotionEvent.TOOL_TYPE_FINGER) {
        // Ignore button code conversion for the unsupported tool type
        return w3cButton ?: 0
    }
    var androidButton = MOUSE_BUTTON_LEFT
    if (w3cButton != null) {
        androidButton = w3cButton
    }
    return when (androidButton) {
        MOUSE_BUTTON_LEFT -> if (toolType == MotionEvent.TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) MotionEvent.BUTTON_STYLUS_PRIMARY else MotionEvent.BUTTON_PRIMARY
        MOUSE_BUTTON_MIDDLE -> MotionEvent.BUTTON_TERTIARY
        MOUSE_BUTTON_RIGHT -> if (toolType == MotionEvent.TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) MotionEvent.BUTTON_STYLUS_SECONDARY else MotionEvent.BUTTON_SECONDARY
        else -> androidButton
    }
}

/**
 * Return Android tool type based on W3C pointer type
 * @param pointerType W3C pointer types (TOUCH, PEN, MOUSE)
 * @return Android Motion Event type (FINGER, STYLUS, MOUSE)
 */
@Throws(AppiumException::class)
fun getToolType(pointerType: InputSource.PointerType?): Int {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return if (pointerType == null) {
        MotionEvent.TOOL_TYPE_FINGER
    } else when (pointerType) {
        InputSource.PointerType.TOUCH -> MotionEvent.TOOL_TYPE_FINGER
        InputSource.PointerType.PEN -> MotionEvent.TOOL_TYPE_STYLUS
        InputSource.PointerType.MOUSE -> MotionEvent.TOOL_TYPE_MOUSE
        else -> throw AppiumException(String.format("Invalid tool type: %s", pointerType))
    }
}

// If a 'mouse' event was provided, convert it to 'touch'
// This is because some clients only send 'mouse' events and the assumption is that if they
// send 'mouse' events to a device that has a touch screen, it needs to be converted
fun isTouch(type: InputSource.PointerType?): Boolean {
    // return type == TOUCH || (type == MOUSE && isTouchScreen); // TODO Revisit this if we wish to support MOUSE on Android
    return type === InputSource.PointerType.TOUCH || type === InputSource.PointerType.MOUSE
}

/**
 * Convert [x,y] coordinates from float to long.
 *
 * Gives warning if the long values are different from the float values
 *
 * @param x X coordinate
 * @param y Y coordinate
 * @return Rounded x and y coordinates
 */
fun toCoordinates(x: Float, y: Float): Point {
    return Point(x.roundToInt(), y.roundToInt())
}