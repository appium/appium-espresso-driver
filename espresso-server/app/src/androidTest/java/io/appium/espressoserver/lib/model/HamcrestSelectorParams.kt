package io.appium.espressoserver.lib.model

@SuppressWarnings("unused")
data class HamcrestSelectorParams(
        val type:String,
        val matcher: HamcrestMatcher
) : AppiumParams()
