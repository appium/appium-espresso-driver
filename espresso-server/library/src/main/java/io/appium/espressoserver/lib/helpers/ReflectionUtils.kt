/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.helpers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.reflection.MethodUtils
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

object ReflectionUtils {

    fun extractMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        try {
            val result = clazz.getDeclaredMethod(methodName, *parameterTypes)
            result.isAccessible = true
            return result
        } catch (e: NoSuchMethodException) {
            throw AppiumException("Method '${methodName}' is not defined for class ${clazz.canonicalName}", e)
        }
    }

    fun invokeStaticMethod(clazz: Class<*>, methodName: String, vararg providedParams: Any?): Any? =
            MethodUtils.invokeStaticMethod(clazz, methodName, *providedParams)

    fun invokeInstanceMethod(instance: Any, methodName: String, vararg providedParams: Any?): Any? =
            MethodUtils.invokeMethod(instance, methodName, *providedParams)

    fun invokeMethod(instance: Any?, method: Method, vararg providedParams: Any?): Any? =
            method.invoke(instance, *providedParams)

    fun extractField(clazz: Class<*>, fieldName: String, instance: Any?): Any? {
        val field: Field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance)
    }

    fun extractDeclaredProperties(instance: Any): Map<String, Any?> {
        return instance::class.declaredMemberProperties
                .fold(mutableMapOf<String, Any?>()) { acc, prop ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        acc[prop.name] = (prop as KProperty1<Any, Any?>).get(instance)
                    } catch (ign: Exception) {
                    }
                    acc
                }.toMap()
    }
}
