package io.appium.espressoserver.lib.helpers.w3c.state

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import java.util.*

/**
 * Pointer input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
class PointerInputState : InputState {
    private val pressed: MutableSet<Int> = HashSet()
    var x = 0f
    var y = 0f
    private var _type: InputSource.PointerType? = null
    @Suppress("SuspiciousVarProperty")
    var type: InputSource.PointerType? = null
        get() { return _type ?: InputSource.PointerType.TOUCH }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(type: InputSource.PointerType?) {
        this._type = type
    }

    fun isPressed(num: Int): Boolean {
        return pressed.contains(num)
    }

    fun addPressed(num: Int) {
        pressed.add(num)
    }

    fun removePressed(num: Int) {
        pressed.remove(num)
    }

    val buttons: Set<Int>
        get() = pressed

    fun hasPressedButtons(): Boolean {
        return pressed.isNotEmpty()
    }

    fun logMessage(): String {
        return String.format(
                "pointer-type=[%s] x=[%s] y=[%s] pressed=[%s]",
                type, x, y, pressed
        )
    }

}