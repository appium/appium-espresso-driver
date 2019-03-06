package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.helpers.BackdoorUtils

data class MobileBackdoorMethod (
    var name: String? = null,
    var args: List<BackdoorMethodArg> = emptyList()
) {
    val argumentTypes: Array<Class<*>?>
        get() {
            val rawArgs = args
            val types = arrayOfNulls<Class<*>>(rawArgs.size)
            for (i in rawArgs.indices) {
                types[i] = BackdoorUtils.parseType(rawArgs[i].type!!)
            }
            return types
        }

    val arguments: Array<Any?>
        get() {
            val rawArgs = args
            val parsedArgs = arrayOfNulls<Any>(rawArgs.size)
            for (i in rawArgs.indices) {
                parsedArgs[i] = BackdoorUtils.parseValue(rawArgs[i].value,
                        BackdoorUtils.parseType(rawArgs[i].type))
            }
            return parsedArgs
        }
}