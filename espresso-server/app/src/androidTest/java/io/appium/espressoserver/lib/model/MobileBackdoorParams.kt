package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

class MobileBackdoorParams : AppiumParams() {
    val target: InvocationTarget? = null
    override var elementId: String? = null

    val methods: List<MobileBackdoorMethod>? = null

    enum class InvocationTarget {
        @SerializedName("activity")
        ACTIVITY,
        @SerializedName("application")
        APPLICATION,
        @SerializedName("element")
        ELEMENT
    }

}

