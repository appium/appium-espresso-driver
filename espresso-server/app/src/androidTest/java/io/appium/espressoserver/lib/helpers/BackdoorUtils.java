package io.appium.espressoserver.lib.helpers;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.model.BackdoorMethodArgs;

public class BackdoorUtils {

    public static Object parseValue(Object o, Class<?> c) {
        if (o == null) return null;
        if (c.equals(boolean.class)) return Boolean.parseBoolean(o.toString());
        if (c.equals(byte.class)) return Byte.parseByte(o.toString());
        if (c.equals(short.class)) return Short.parseShort(o.toString());
        if (c.equals(int.class)) return Integer.parseInt(o.toString());
        if (c.equals(long.class)) return Long.parseLong(o.toString());
        if (c.equals(float.class)) return Float.parseFloat(o.toString());
        if (c.equals(double.class)) return Double.parseDouble(o.toString());
        if (c.equals(char.class)) return o.toString().charAt(0);
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
                String fqn = className.contains(".") ? className : "java.lang.".concat(className);
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + fqn);
                }
        }
    }


    public static List<BackdoorMethodArgs> parseArguments(List<BackdoorMethodArgs> arguments) {
        for (BackdoorMethodArgs methodArgs : arguments) {
            Class parsedType = parseType(methodArgs.getType());
            Object parsedValue = parseValue(methodArgs.getValue(), parsedType);
            methodArgs.setParsedType(parsedType);
            methodArgs.setParsedValue(parsedValue);
        }
        return arguments;
    }

    public static Class<?>[] getParsedTypes(List<BackdoorMethodArgs> arguments) {
        Class<?>[] types = new Class<?>[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            types[i] = arguments.get(i).getParsedType();
        }
        return types;
    }

    public static List<Object> getParsedValues(List<BackdoorMethodArgs> arguments) {
        List<Object> values = new ArrayList<>();
        for (BackdoorMethodArgs methodArgs : arguments) {
            values.add(methodArgs.getParsedValue());
        }
        return values;
    }

}
