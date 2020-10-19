package io.appium.espressoserver.lib.helpers.w3c.state

import android.view.KeyEvent
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

    fun toMetaState(): Int {
        var metaState = 0
        if (isAlt) {
            metaState = metaState or KeyEvent.META_ALT_MASK
        }
        if (isCtrl) {
            metaState = metaState or KeyEvent.META_CTRL_MASK
        }
        if (isShift) {
            metaState = metaState or KeyEvent.META_SHIFT_MASK
        }
        if (isMeta) {
            metaState = metaState or KeyEvent.META_META_MASK
        }
        return metaState
    }
}