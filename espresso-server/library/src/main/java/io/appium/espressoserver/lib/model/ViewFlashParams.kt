package io.appium.espressoserver.lib.model

val DURATION_MILLIS = 30
val REPEAT_COUNT = 15

data class ViewFlashParams(
    val durationMillis: Int = DURATION_MILLIS,
    val repeatCount: Int = REPEAT_COUNT
) : AppiumParams()
