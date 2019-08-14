package io.appium.espressoserver.lib.model

data class ElementValueParams(
    val value: List<String>? = null,
    val text: String? = null
) : AppiumParams()