package io.appium.espressoserver.lib.model.web

import java.util.*

class WebAtoms {
    var webviewElement: String? = null
    var forceJavascriptEnabled: Boolean = false
    var methodChain: List<WebAtomsMethod> = Collections.emptyList()

    class WebAtomsMethod {
        var name: String? = null
        var atom: WebAtom? = null
    }
}