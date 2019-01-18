package io.appium.espressoserver.lib.helpers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

object KReflectionUtils {

    fun invokeMethod(kclass: KClass<*>, methodName: String, vararg providedParams: Any): Any? {
        val treatedParams = providedParams.clone().toMutableList()
        for (func in kclass.memberFunctions) {
            // Look for function names that match provided methodName
            if (func.name == methodName) {

                // Test that the provided parameters match the function parameters
                val funcParams = func.parameters
                if (funcParams.size != providedParams.size) {
                    continue
                }

                // Find a function that matches the provided parameters
                var isMatch = true
                for (index in 1 until funcParams.size) {
                    val funcParamType = funcParams.get(index).type
                    val providedParam = providedParams.get(index)
                    val providedParamType = providedParams.get(index)::class.createType()

                    // Hack Enum Case
                    // If function param is Enum and provided param is String, try `enumValueOf` on that String value
                    try {
                        val jFuncType = funcParamType.javaType as Class<*>
                        if (jFuncType.isEnum && providedParam is String) {
                            val enumValueOf = jFuncType.getDeclaredMethod("valueOf", String::class.java)
                            treatedParams.set(index, enumValueOf(null, providedParam.toUpperCase()))
                            continue;
                        }
                    } catch (e:Exception) {
                        // Ignore exceptions and try matching String -> String
                    }

                    // If this function param doesn't match what's provided, skip to next
                    if (funcParamType != providedParamType) {
                        isMatch = false
                        break
                    }
                }

                if (isMatch) {
                    return func.call(*treatedParams.toTypedArray())
                }
            }
        }

        throw AppiumException("Could not find method to invoke: class=[${kclass.qualifiedName}] " +
                "methodName=[${methodName}] args=[${providedParams.joinToString(", ")}]");
    }

    fun invokeInstanceMethod (instance: Any, methodName: String, vararg providedParams: Any): Any? {
        return invokeMethod(instance::class, methodName, instance, *providedParams);
    }
}
