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

    fun logMessage(): String {
        return "key=[$key] code=[$code] altKey=[$isAltKey] shiftKey=[$isShiftKey] ctrlKey=[$isCtrlKey] metaKey=[$isMetaKey]"
    }

}