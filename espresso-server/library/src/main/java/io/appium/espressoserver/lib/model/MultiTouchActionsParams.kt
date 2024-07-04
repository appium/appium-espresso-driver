package io.appium.espressoserver.lib.model

data class MultiTouchActionsParams(
    val actions: List<List<TouchAction>> = emptyList()
) : AppiumParams()