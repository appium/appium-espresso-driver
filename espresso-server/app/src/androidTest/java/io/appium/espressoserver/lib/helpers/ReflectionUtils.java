package io.appium.espressoserver.lib.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

public class ReflectionUtils {
    public static Object getField(final Class clazz, final String fieldName, final Object object)
            throws AppiumException {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.get(object);
        } catch (final Exception e) {
            final String msg = String.format("error while getting field %s from object %s",
                    fieldName, object);
            logger.error(msg + " " + e.getMessage());
            throw new AppiumException(msg, e);
        }
    }

    public static Object invoke(final Method method, final Object object, final Object... parameters)
            throws AppiumException {
        try {
            return method.invoke(object, parameters);
        } catch (final Exception e) {
            final String msg = String.format(
                    "error while invoking method %s on object %s with parameters %s",
                    method, object, Arrays.toString(parameters));
            logger.error(msg + " " + e.getMessage());
            throw new AppiumException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Method method(final Class clazz, final String methodName,
                                 final Class... parameterTypes) throws AppiumException {
        try {
            final Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);

            return method;
        } catch (final Exception e) {
            final String msg = String.format(
                    "error while getting method %s from class %s with parameter types %s",
                    methodName, clazz, Arrays.toString(parameterTypes));
            logger.error(msg + " " + e.getMessage());
            throw new AppiumException(msg, e);
        }
    }

    private static Class getClass(final String name) throws AppiumException {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException e) {
            final String msg = String.format("unable to find class %s", name);
            throw new AppiumException(msg, e);
        }
    }

    public static Method method(final String className, final String method,
                                 final Class... parameterTypes) throws AppiumException {
        return method(getClass(className), method, parameterTypes);
    }
}
