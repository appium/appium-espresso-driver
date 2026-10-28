package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

const val DURATION_MILLIS = 30
const val REPEAT_COUNT = 15

data class ViewFlashParams(
    @SerializedName("durationMillis")
    private val _durationMillis: Int? = null,
    @SerializedName("repeatCount")
    private val _repeatCount: Int? = null
) : AppiumParams() {
    val durationMillis: Int
        get() = _durationMillis ?: DURATION_MILLIS
    val repeatCount: Int
        get() = _repeatCount ?: REPEAT_COUNT
}
