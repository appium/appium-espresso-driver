package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.helpers.BackdoorUtils

data class MobileBackdoorMethod(val name: String? = null, var args: List<BackdoorMethodArg> = emptyList()) {
    val argumentTypes: Array<Class<*>?>
        get() {
            val types = arrayOfNulls<Class<*>>(args.size)
            for (i in args.indices) {
                types[i] = BackdoorUtils.parseType(args[i].type)
            }
            return types
        }

    val arguments: Array<Any?>
        get() {
            val parsedArgs = arrayOfNulls<Any>(args.size)
            for (i in args.indices) {
                parsedArgs[i] = BackdoorUtils.parseValue(args[i].value,
                        BackdoorUtils.parseType(args[i].type))
            }
            return parsedArgs
        }
}