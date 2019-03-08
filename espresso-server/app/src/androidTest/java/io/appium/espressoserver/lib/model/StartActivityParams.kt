package io.appium.espressoserver.lib.model

data class StartActivityParams(
    val appActivity: String? = null,
    val appWaitActivity: String? = null
) : AppiumParams()
