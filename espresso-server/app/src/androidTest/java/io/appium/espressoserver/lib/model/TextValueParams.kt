package io.appium.espressoserver.lib.model

data class TextValueParams(
    val value: List<String>? = null,
    val text: String? = null
) : AppiumParams()