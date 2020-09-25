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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassUtils {

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
    static {
        for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
            final Class<?> primitiveClass = entry.getKey();
            final Class<?> wrapperClass = entry.getValue();
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        return wrapperPrimitiveMap.get(cls);
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, final boolean autoboxing) {
        if (!ArrayUtils.isSameLength(classArray, toClassArray)) {
            return false;
        }
        if (classArray == null) {
            classArray = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if (toClassArray == null) {
            toClassArray = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        for (int i = 0; i < classArray.length; i++) {
            if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("DuplicateExpressions")
    public static boolean isAssignable(Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        //autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass)
                        || Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            // should never get here
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static Class<?>[] toClass(final Object... array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Class<?>[] classes = new Class[array.length];
        for (int i = 0; i < array.length; i++) {
            classes[i] = array[i] == null ? null : array[i].getClass();
        }
        return classes;
    }
}
