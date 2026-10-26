package io.appium.espressoserver.lib.model.web

import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.model.AppiumParams

data class WebAtomsParams(
    val webviewEl: String = "",
    @SerializedName("forceJavascriptEnabled")
    private val _forceJavascriptEnabled: Boolean? = null,
    @SerializedName("methodChain")
    private val _methodChain: List<WebAtomsMethod>? = null
) : AppiumParams() {
    val forceJavascriptEnabled: Boolean
        get() = _forceJavascriptEnabled ?: false

    val methodChain: List<WebAtomsMethod>
        get() = _methodChain ?: emptyList()
}