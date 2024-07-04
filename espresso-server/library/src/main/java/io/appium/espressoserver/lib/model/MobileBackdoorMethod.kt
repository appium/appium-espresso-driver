package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.helpers.BackdoorUtils

data class MobileBackdoorMethod (
    var name: String? = null,
    var args: List<BackdoorMethodArg> = emptyList()
) {
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