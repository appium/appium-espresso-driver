package io.appium.espressoserver.lib.model

data class ViewText(val rawText: String?, val isHint: Boolean) {

    constructor(rawText: Int) : this(Integer.toString(rawText), false)
}
