package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

data class MultiTouchActionsParams(
    @SerializedName("actions")
    private val _actions: List<List<TouchAction>>? = null
) : AppiumParams() {
    val actions: List<List<TouchAction>>
        get() = _actions ?: emptyList()
}