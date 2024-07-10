package io.appium.espressoserver.lib.helpers

/**
 * Copies parts of the Android Rect
 *
 * This is copied so that we can run in Java context
 */
class Rect(private val left: Int, private val top: Int, private val right: Int, private val bottom: Int) {

    fun contains(x: Int, y: Int): Boolean {
        return (left < right && top < bottom  // check for empty first
                && x >= left && x < right && y >= top && y < bottom)
    }

    override fun toString(): String {
        return "Rect($left, $top, $right, $bottom)"
    }

    fun toShortString(): String {
        return "Rect[$left, $top, $right, $bottom]"
    }
}
