package io.appium.espressoserver.lib.helpers;

import java.util.List;

public class BackdoorUtils {
    public static Class<?> mapToPrimitiveClass(Class<?> c) {
        if (c.equals(Integer.class)) return int.class;
        if (c.equals(Float.class)) return float.class;
        if (c.equals(Double.class)) return double.class;
        if (c.equals(Boolean.class)) return boolean.class;
        if (c.equals(Long.class)) return long.class;
        if (c.equals(Byte.class)) return byte.class;
        if (c.equals(Short.class)) return short.class;
        if (c.equals(Character.class)) return char.class;

        return c;
    }

    public static Class<?> mapToBoxingClass(Class<?> c) {
        if (c.equals(int.class)) return Integer.class;
        if (c.equals(float.class)) return Float.class;
        if (c.equals(double.class)) return Double.class;
        if (c.equals(boolean.class)) return Boolean.class;
        if (c.equals(long.class)) return Long.class;
        if (c.equals(byte.class)) return Byte.class;
        if (c.equals(short.class)) return Short.class;
        if (c.equals(char.class)) return Character.class;

        return c;
    }

    public static Object numericValue(Object o, Class<?> c) {
        if (o == null) return null;

        if (c.equals(Integer.class)) return Integer.parseInt(o.toString());
        if (c.equals(Float.class)) return Float.parseFloat(o.toString());
        if (c.equals(Double.class)) return Double.parseDouble(o.toString());
        if (c.equals(Long.class)) return Long.parseLong(o.toString());
        if (c.equals(Byte.class)) return Byte.parseByte(o.toString());
        if (c.equals(Short.class)) return Short.parseShort(o.toString());

        return o;
    }

    public static Class<?>[] parseArgumentTypes(List<?> arguments) {
        Class<?>[] types = new Class<?>[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            types[i] = arguments.get(i) == null ? Object.class : arguments.get(i).getClass();
        }

        return types;
    }
}
