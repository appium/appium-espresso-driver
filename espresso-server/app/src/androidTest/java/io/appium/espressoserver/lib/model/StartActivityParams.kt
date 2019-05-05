package io.appium.espressoserver.lib.model

data class StartActivityParams(
    val appPackage: String? = null,
    val appActivity: String
) : AppiumParams()
