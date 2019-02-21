package io.appium.espressoserver.lib.model

data class ViewFlashParams(
    val durationMillis: Int? = null,
    val repeatCount: Int? = null
) : AppiumParams()
