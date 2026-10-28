package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName
import io.appium.espressoserver.lib.helpers.BackdoorUtils

data class MobileBackdoorMethod (
    var name: String? = null,
    @SerializedName("args")
    private var _args: List<BackdoorMethodArg>? = null
) {
    var args: List<BackdoorMethodArg>
        get() = _args ?: emptyList()
        set(value) {
            _args = value
        }

    val argumentTypes: Array<Class<*>?>
        get() {
            return args.indices.map {
                BackdoorUtils.parseType(args[it].type)
            }.toTypedArray()
        }

    val arguments: Array<Any?>
        get() {
            return args.indices.map {
                BackdoorUtils.parseValue(args[it].value, BackdoorUtils.parseType(args[it].type))
            }.toTypedArray()
        }
}