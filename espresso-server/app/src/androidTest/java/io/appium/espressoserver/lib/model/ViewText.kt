package io.appium.espressoserver.lib.model

data class ViewText(
        val rawText: String = "",
        val isHint: Boolean = false,
        val textColor: Int?=null,
        val elevation: Float?=null
) {
    constructor(rawText: Int) : this(Integer.toString(rawText), false)

    override fun toString(): String {
        return rawText
    }
}
