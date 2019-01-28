package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

data class MobileBackdoorParams(
    val target: InvocationTarget?,
    val methods: List<MobileBackdoorMethod>?
)  : AppiumParams() {
    //override var elementId: String? = null

    enum class InvocationTarget {
        @SerializedName("activity")
        ACTIVITY,
        @SerializedName("application")
        APPLICATION,
        @SerializedName("element")
        ELEMENT
    }
}

