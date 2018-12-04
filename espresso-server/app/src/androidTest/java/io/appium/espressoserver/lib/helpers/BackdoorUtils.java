package io.appium.espressoserver.lib.helpers;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.model.MobileBackdoorMethod;

public class BackdoorUtils {

    public static Object parseValue(Object o, Class<?> c) {
        if (o == null) return null;
        String value = o.toString();
        if (c.equals(boolean.class) || c.equals(Boolean.class)) return Boolean.parseBoolean(value);
        if (c.equals(byte.class) || c.equals(Byte.class)) return Byte.parseByte(value);
        if (c.equals(short.class) || c.equals(Short.class)) return Short.parseShort(value);
        if (c.equals(int.class) || c.equals(Integer.class)) return Integer.parseInt(value);
        if (c.equals(long.class) || c.equals(Long.class)) return Long.parseLong(value);
        if (c.equals(float.class) || c.equals(Float.class)) return Float.parseFloat(value);
        if (c.equals(double.class) || c.equals(Double.class)) return Double.parseDouble(value);
        if (c.equals(char.class) || c.equals(Character.class)) return value.charAt(0);
        return o;
    }

    public static Class<?> parseType(final String className) {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                String fqn = className.contains(".") ? className : "java.lang." + className;
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + fqn, ex);
                }
        }
    }

    @Nullable
    public static Object invokeMethods(Object invokeOn, List<InvocationOperation> ops) throws AppiumException {
        Object invocationResult = null;
        Object invocationTarget = invokeOn;
        for (InvocationOperation op : ops) {
            try {
                invocationResult = op.apply(invocationTarget);
                invocationTarget = invocationResult;
            } catch (Exception e) {
                throw new AppiumException(e);
            }
        }
        return invocationResult;
    }

    public static List<InvocationOperation> getOperations(List<MobileBackdoorMethod> mobileBackdoorMethods) throws InvalidArgumentException {
        List<InvocationOperation> ops = new ArrayList<>();

        for (MobileBackdoorMethod mobileBackdoorMethod : mobileBackdoorMethods) {
            String methodName = mobileBackdoorMethod.getName();
            if (methodName == null) {
                throw new InvalidArgumentException("'name' is a required parameter for backdoor method to be invoked.");
            }
            ops.add(new InvocationOperation(methodName, mobileBackdoorMethod.getArguments(),
                    mobileBackdoorMethod.getArgumentTypes()));
        }
        return ops;
    }
}
