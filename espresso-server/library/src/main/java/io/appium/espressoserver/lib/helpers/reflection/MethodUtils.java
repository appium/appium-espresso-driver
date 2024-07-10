/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.helpers.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Copied from org.apache.commons.lang3.reflect
 */
public class MethodUtils {

    public static Object invokeMethod(final Object object, final String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        Method method = getMatchingMethod(object.getClass(), methodName, parameterTypes);
        if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
        }

        if (method == null) {
            throw new NoSuchMethodException("No such method: " + methodName + "() on object: " + object.getClass().getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(object, args);
    }

    public static Object invokeStaticMethod(final Class<?> cls, final String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        final Method method = getMatchingMethod(cls, methodName, parameterTypes);
        if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
        }

        if (method == null) {
            throw new NoSuchMethodException("No such method: " + methodName + "() on class: " + cls.getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(null, args);
    }


    private static Object[] getVarArgs(final Object[] args, final Class<?>[] methodParameterTypes) {
        if (args.length == methodParameterTypes.length && (args[args.length - 1] == null ||
                args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1]))) {
            // The args array is already in the canonical form for the method.
            return args;
        }

        // Construct a new array matching the method's declared parameter types.
        final Object[] newArgs = new Object[methodParameterTypes.length];

        // Copy the normal (non-varargs) parameters
        System.arraycopy(args, 0, newArgs, 0, methodParameterTypes.length - 1);

        // Construct a new array for the variadic parameters
        final Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
        final int varArgLength = args.length - methodParameterTypes.length + 1;

        Object varArgsArray = Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength);
        // Copy the variadic arguments into the varargs array.
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(args, methodParameterTypes.length - 1, varArgsArray, 0, varArgLength);

        //noinspection ConstantConditions
        if (varArgComponentType.isPrimitive()) {
            // unbox from wrapper type to primitive type
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }

        // Store the varargs array in the last position of the array to return
        newArgs[methodParameterTypes.length - 1] = varArgsArray;

        // Return the canonical varargs array.
        return newArgs;
    }

    private static Object[] toVarArgs(final Method method, Object[] args) {
        if (method.isVarArgs()) {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            args = getVarArgs(args, methodParameterTypes);
        }
        return args;
    }

    private static Method getMatchingMethod(final Class<?> cls, final String methodName, final Class<?>... parameterTypes) {
        Validate.notNull(cls, "Null class not allowed.");
        Validate.notEmpty(methodName, "Null or blank methodName not allowed.");

        // Address methods in superclasses
        Method[] methodArray = cls.getDeclaredMethods();
        final List<Class<?>> superclassList = ClassUtils.getAllSuperclasses(cls);
        for (final Class<?> klass : superclassList) {
            methodArray = ArrayUtils.addAll(methodArray, klass.getDeclaredMethods());
        }

        Method inexactMatch = null;
        for (final Method method : methodArray) {
            if (methodName.equals(method.getName()) &&
                    Objects.deepEquals(parameterTypes, method.getParameterTypes())) {
                return method;
            } else if (methodName.equals(method.getName()) &&
                    ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                if ((inexactMatch == null) || (distance(parameterTypes, method.getParameterTypes())
                        < distance(parameterTypes, inexactMatch.getParameterTypes()))) {
                    inexactMatch = method;
                }
            }
        }
        return inexactMatch;
    }

    private static int distance(final Class<?>[] classArray, final Class<?>[] toClassArray) {
        int answer = 0;

        if (!ClassUtils.isAssignable(classArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < classArray.length; offset++) {
            // Note InheritanceUtils.distance() uses different scoring system.
            if (classArray[offset].equals(toClassArray[offset])) {
                //noinspection UnnecessaryContinue
                continue;
            } else if (ClassUtils.isAssignable(classArray[offset], toClassArray[offset], true)
                    && !ClassUtils.isAssignable(classArray[offset], toClassArray[offset], false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }

        return answer;
    }
}
