package io.appium.espressoserver.lib.model

import java.util.Collections

import io.appium.espressoserver.lib.helpers.BackdoorUtils

class MobileBackdoorMethod {
    val name: String? = null

    private var args: List<BackdoorMethodArg>? = null

    val rawArgs: List<BackdoorMethodArg>?
        get() = if (args == null) {
            emptyList()
        } else args

    val argumentTypes: Array<Class<*>?>
        get() {
            val rawArgs = rawArgs
            val types = arrayOfNulls<Class<*>>(rawArgs!!.size)
            for (i in rawArgs.indices) {
                types[i] = BackdoorUtils.parseType(rawArgs[i].type!!)
            }
            return types
        }

    val arguments: Array<Any?>
        get() {
            val rawArgs = rawArgs
            val parsedArgs = arrayOfNulls<Any>(rawArgs!!.size)
            for (i in rawArgs.indices) {
                parsedArgs[i] = BackdoorUtils.parseValue(rawArgs[i].value,
                        BackdoorUtils.parseType(rawArgs[i].type!!))
            }
            return parsedArgs
        }

    fun setArgs(args: List<BackdoorMethodArg>) {
        this.args = args
    }
}