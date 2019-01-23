package io.appium.espressoserver.lib.helpers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import java.lang.ClassCastException
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

object KReflectionUtils {

    fun invokeMethod(functions: Collection<KFunction<*>>, methodName: String, vararg providedParams: Any): Any? {
        val treatedParams = providedParams.clone().toMutableList()
        for (func in functions) {
            // Look for function names that match provided methodName
            if (func.name == methodName) {

                // If the length of func parameters provided isn't same as expected, go to next.
                val funcParams = func.parameters
                if (funcParams.size != providedParams.size) {
                    continue
                }

                // Look through function parameters and do an enum hack to translate strings to enums
                funcParams.forEachIndexed { index, funcParam ->
                    val providedParam = providedParams.get(index)

                    // Hack Enum Case
                    // If function param is Enum and provided param is String, try `enumValueOf` on that String value
                    try {
                        val jFuncType = funcParam.type.javaType as Class<*>
                        if (jFuncType.isEnum && providedParam is String) {
                            val enumValueOf = jFuncType.getDeclaredMethod("valueOf", String::class.java)
                            treatedParams.set(index, enumValueOf(null, providedParam.toUpperCase()))
                        }
                    } catch (e:ReflectiveOperationException) {
                        // Ignore reflection exceptions and don't try matching String to Enum
                    } catch (e:ClassCastException) {
                        // Ignore class cast exceptions and don't try matching String to Enum
                    }
                }

                // Attempt to call this function. If it fails, try the next function definition.
                try {
                    return func.call(*treatedParams.toTypedArray())
                } catch (e:IllegalArgumentException) {
                    // If IllegalArguments that means parameters didn't match, move on to the next
                }
            }
        }

        throw AppiumException("Could not invoke method: " +
                "methodName=[${methodName}] args=[${providedParams.joinToString(", ")}]")
    }

    fun invokeMethod(kclass: KClass<*>, methodName: String, vararg providedParams: Any): Any? {
        return invokeMethod(kclass.functions, methodName, *providedParams)
    }

    fun invokeInstanceMethod (instance: Any, methodName: String, vararg providedParams: Any): Any? {
        return invokeMethod(instance::class.memberFunctions, methodName, instance, *providedParams)
    }
}
