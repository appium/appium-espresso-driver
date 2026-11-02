package io.appium.espressoserver.lib.helpers.w3c.dispatcher

class W3CKeyEvent {
    var key: String? = null
    var code: String? = null
    var location = 0
    var keyCode = 0
    var charCode = 0
    var which = 0
    var isAltKey = false
    var isShiftKey = false
    var isCtrlKey = false
    var isMetaKey = false
    var isRepeat = false
    var isComposing = false

    // Typed characters may be sensitive, so neither key nor code are logged
    fun logMessage(): String {
        val keyKind = if (key?.firstOrNull() in '\uE000'..'\uF8FF') "special" else "character"
        return "key=[$keyKind] altKey=[$isAltKey] shiftKey=[$isShiftKey] ctrlKey=[$isCtrlKey] metaKey=[$isMetaKey]"
    }

}