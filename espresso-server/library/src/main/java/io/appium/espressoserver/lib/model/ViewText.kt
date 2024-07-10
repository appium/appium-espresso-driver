package io.appium.espressoserver.lib.model

data class ViewText(
    val rawText: String = "",
    val isHint: Boolean = false
) {
    constructor(rawText: Int) : this(Integer.toString(rawText), false)

    override fun toString(): String {
        return rawText
    }
}
