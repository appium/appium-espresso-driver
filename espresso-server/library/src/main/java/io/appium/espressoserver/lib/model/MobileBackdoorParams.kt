package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

enum class InvocationTarget {
    @SerializedName("activity")
    ACTIVITY,
    @SerializedName("application")
    APPLICATION,
    @SerializedName("element")
    ELEMENT
}

data class MobileBackdoorParams(
        val target: InvocationTarget? = null,
        var targetElement: String? = null,
        val methods: List<MobileBackdoorMethod> = emptyList()

) : AppiumParams()

