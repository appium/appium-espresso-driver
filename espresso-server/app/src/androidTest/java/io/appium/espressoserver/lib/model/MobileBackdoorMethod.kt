package io.appium.espressoserver.lib.model

import io.appium.espressoserver.lib.helpers.BackdoorUtils

data class MobileBackdoorMethod(
        val name: String?,
        var args: List<BackdoorMethodArg>?
) {
    val argumentTypes: Array<Class<*>?>
        get() {
            args?.let {
                val types = arrayOfNulls<Class<*>>(it.size)
                for (i in it.indices) {
                    types[i] = BackdoorUtils.parseType(it[i].type)
                }
                return types
            }
            return emptyArray()
        }

    val arguments: Array<Any?>
        get() {
            args?.let {
                val parsedArgs = arrayOfNulls<Any>(it.size)
                for (i in it.indices) {
                    parsedArgs[i] = BackdoorUtils.parseValue(
                            it[i].value,
                            BackdoorUtils.parseType(it[i].type)
                    )
                }
                return parsedArgs
            }
            return emptyArray()
        }
}