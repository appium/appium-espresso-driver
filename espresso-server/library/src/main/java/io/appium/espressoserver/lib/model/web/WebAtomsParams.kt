package io.appium.espressoserver.lib.model.web

import io.appium.espressoserver.lib.model.AppiumParams

data class WebAtomsParams(
        val webviewEl: String,
        val forceJavascriptEnabled: Boolean,
        val methodChain: List<WebAtomsMethod> = emptyList()
) : AppiumParams()