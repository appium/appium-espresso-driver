package io.appium.espressoserver.lib.helpers

import com.google.gson.internal.LazilyParsedNumber
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

object KReflectionUtils {

    fun invokeMethod(functions: Collection<KFunction<*>>, methodName: String, vararg providedParams: Any?): Any? {
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
                    val providedParam = providedParams[index]

                    // Handle the Enum Case
                    // If function param is Enum and provided param is String, try `enumValueOf` on that String value
                    val type = funcParam.type
                    try {
                        val jFuncType = type.javaType as Class<*>
                        if (jFuncType.isEnum && providedParam is String) {
                            val enumValueOf = jFuncType.getDeclaredMethod("valueOf", String::class.java)
                            treatedParams[index] = enumValueOf(null, providedParam.toUpperCase())
                        }
                    } catch (e:ReflectiveOperationException) {
                        // Ignore reflection exceptions and don't try matching String to Enum
                    } catch (e:ClassCastException) {
                        // Ignore class cast exceptions and don't try matching String to Enum
                    }

                    // Handle the Class case
                    val classifier = type.classifier
                    if (classifier is KClass<*> && classifier.isSubclassOf(Class::class)) {
                        var className: String = providedParam.toString()
                        val classExtension = ".class"
                        if (className.endsWith(classExtension)) {
                            className = className.take(className.length - classExtension.length)
                        }

                        try {
                            val clazz = Class.forName(className)
                            treatedParams[index] = clazz
                        } catch (e: ClassNotFoundException) { }

                        try {
                            val clazz = Class.forName("java.lang.${className}")
                            treatedParams[index] = clazz
                        } catch (e: ClassNotFoundException) { }
                    }

                    if (providedParam is LazilyParsedNumber) {
                        treatedParams[index] = providedParam.toDouble()
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

        throw AppiumException("Could not find method that matches " +
                "methodName=[${methodName}] args=[${providedParams.joinToString(", ")}]")
    }

    fun invokeMethod(kclass: KClass<*>, methodName: String, vararg providedParams: Any?): Any? {
        try {
            return invokeMethod(kclass.functions, methodName, *providedParams)
        } catch (e:AppiumException) {
            throw AppiumException("Cannot execute method on '${kclass.qualifiedName}'. Reason: ${e.message}'")
        }
    }

    fun invokeInstanceMethod (instance: Any, methodName: String, vararg providedParams: Any?): Any? {
        try {
            return invokeMethod(instance::class.memberFunctions, methodName, instance, *providedParams)
        } catch (e:AppiumException) {
            throw AppiumException("Cannot execute method for instance of " +
                    "'${instance::class.qualifiedName}'. Reason: ${e.message}'")
        }
    }

    fun extractDeclaredProperties (instance: Any): Map<String, Any?> {
        return instance::class.declaredMemberProperties
                .fold(mutableMapOf<String, Any?>()) { acc, prop ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        acc[prop.name] = (prop as KProperty1<Any, Any?>).get(instance)
                    } catch (ign:Exception) {}
                    acc
                }.toMap()
    }
}
