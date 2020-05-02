package io.appium.espressoserver.lib.helpers.w3c.state

import java.util.*

/**
 * Implement 'calculated global key state' in spec 17.2
 */
fun getGlobalKeyState(keyInputStates: List<KeyInputState>): KeyInputState {
    var isAlt = false
    var isShift = false
    var isCtrl = false
    var isMeta = false
    val outputState = KeyInputState()
    for (keyInputState in keyInputStates) {
        if (keyInputState.isAlt) {
            isAlt = true
        }
        if (keyInputState.isShift) {
            isShift = true
        }
        if (keyInputState.isCtrl) {
            isCtrl = true
        }
        if (keyInputState.isMeta) {
            isMeta = true
        }
        for (key in keyInputState.pressed) {
            outputState.addPressed(key)
        }
    }
    outputState.isAlt = isAlt
    outputState.isShift = isShift
    outputState.isCtrl = isCtrl
    outputState.isMeta = isMeta
    return outputState
}

/**
 * Key input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
class KeyInputState : InputState {
    val pressed: MutableSet<String> = HashSet()
    var isAlt = false
    var isShift = false
    var isCtrl = false
    var isMeta = false
    fun isPressed(key: String): Boolean {
        return pressed.contains(key)
    }

    fun addPressed(key: String) {
        pressed.add(key)
    }

    fun removePressed(key: String) {
        pressed.remove(key)
    }

    fun logMessage(): String {
        return "alt=[$isAlt] shift=[$isShift] ctrl=[$isCtrl] meta=[$isMeta] pressed=[$pressed]"
    }
}