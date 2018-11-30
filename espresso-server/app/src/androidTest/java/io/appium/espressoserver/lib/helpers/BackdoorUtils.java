package io.appium.espressoserver.lib.helpers;

public class BackdoorUtils {

    public static Object parseValue(Object o, Class<?> c) {
        if (o == null) return null;
        String value = o.toString();
        if (c.equals(boolean.class)) return Boolean.parseBoolean(value);
        if (c.equals(byte.class)) return Byte.parseByte(value);
        if (c.equals(short.class)) return Short.parseShort(value);
        if (c.equals(int.class)) return Integer.parseInt(value);
        if (c.equals(long.class)) return Long.parseLong(value);
        if (c.equals(float.class)) return Float.parseFloat(value);
        if (c.equals(double.class)) return Double.parseDouble(value);
        if (c.equals(char.class)) return value.charAt(0);
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

}
