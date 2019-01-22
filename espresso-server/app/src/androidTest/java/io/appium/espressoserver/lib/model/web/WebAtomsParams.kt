package io.appium.espressoserver.lib.model.web

import io.appium.espressoserver.lib.model.AppiumParams
import java.util.*

class WebAtomsParams : AppiumParams() {
    var webviewElement: String? = null
    var forceJavascriptEnabled: Boolean = false
    var methodChain: List<WebAtomsMethod> = Collections.emptyList()

    data class WebAtomsMethod(val name: String, val atom: WebAtom)
}