package io.appium.espressoserver.lib.helpers;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.appium.espressoserver.lib.model.BackdoorResultVoid;

//https://github.com/calabash/calabash-android-server/blob/develop/server/app/src/androidTest/java/sh/calaba/instrumentationbackend/query/InvocationOperation.java

public class InvocationOperation {
    private final String methodName;
    private final List<?> arguments;
    private final Class<?>[] argumentTypes;

    public InvocationOperation(String methodName, List<?> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
        this.argumentTypes = parseArgumentTypes(arguments);
    }


    public Object apply(final Object o) throws Exception {
        Log.d("Backdoor Method", getName());
        Log.d("Invoking on ", o.toString());
        final AtomicReference<Object> ref = new AtomicReference<Object>();
        final AtomicReference<Exception> refEx = new AtomicReference<Exception>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                MethodWithArguments method = findCompatibleMethod(o);

                if (method == null) {
                    StringBuilder stringBuilder = new StringBuilder("No such method found: ");
                    stringBuilder.append(InvocationOperation.this.methodName);
                    stringBuilder.append("(");
                    int length = InvocationOperation.this.arguments.size();

                    for (int i = 0; i < length; i++) {
                        Object argument = InvocationOperation.this.arguments.get(i);

                        if (i != 0) {
                            stringBuilder.append(", ");
                        }

                        String simpleName;

                        if (argument == null) {
                            simpleName = "null";
                        } else {
                            simpleName = argument.getClass().getSimpleName();
                        }

                        stringBuilder.append("[").append(simpleName).append("]");
                    }

                    stringBuilder.append(")");

                    ref.set(BackdoorResultVoid.instance
                            .asMap(InvocationOperation.this.methodName, o, stringBuilder.toString()));
                    return;
                }

                Object result;

                try {
                    if (method.getMethod().getReturnType().equals(Void.TYPE)) {
                        method.invoke(o);
                        result = "<VOID>";
                    } else {
                        result = method.invoke(o);
                    }

                    ref.set(result);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    refEx.set(e);
                    return;
                }
            }
        });

        if (refEx.get() != null) {
            throw refEx.get();
        }

        return ref.get();
    }

    public MethodWithArguments findCompatibleMethod(Object object) {
        return findCompatibleMethod(object.getClass());
    }

    public MethodWithArguments findCompatibleMethod(Class<?> forClass) {
        // Fast path
        try {
            return new MethodWithArguments(forClass.getMethod(methodName, argumentTypes),
                    new ArrayList<Object>(arguments));
        } catch (NoSuchMethodException e) {
            // No immediate method found

            for (Method method : forClass.getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                try {
                    return new MethodWithArguments(method, parseToSuitableArguments(method));
                } catch (IncompatibleArgumentsException e1) {
                    continue;
                }
            }

            return null;
        }
    }

    private static Class<?> mapToPrimitiveClass(Class<?> c) {
        if (c.equals(Integer.class)) {
            return int.class;
        } else if (c.equals(Float.class)) {
            return float.class;
        } else if (c.equals(Double.class)) {
            return double.class;
        } else if (c.equals(Boolean.class)) {
            return boolean.class;
        } else if (c.equals(Long.class)) {
            return long.class;
        } else if (c.equals(Byte.class)) {
            return byte.class;
        } else if (c.equals(Short.class)) {
            return short.class;
        } else if (c.equals(Character.class)) {
            return char.class;
        }

        return c;
    }

    private static Class<?> mapToBoxingClass(Class<?> c) {
        if (c.equals(int.class)) {
            return Integer.class;
        } else if (c.equals(float.class)) {
            return Float.class;
        } else if (c.equals(double.class)) {
            return Double.class;
        } else if (c.equals(boolean.class)) {
            return Boolean.class;
        } else if (c.equals(long.class)) {
            return Long.class;
        } else if (c.equals(byte.class)) {
            return Byte.class;
        } else if (c.equals(short.class)) {
            return Short.class;
        } else if (c.equals(char.class)) {
            return Character.class;
        }

        return c;
    }

    private static Object primitiveValue(Object o) {
        if (o == null) {
            return null;
        }

        Class<?> c = o.getClass();

        if (c.equals(Integer.class)) {
            return ((Integer) o).intValue();
        } else if (c.equals(Float.class)) {
            return ((Float) o).floatValue();
        } else if (c.equals(Double.class)) {
            return ((Double) o).doubleValue();
        } else if (c.equals(Boolean.class)) {
            return ((Boolean) o).booleanValue();
        } else if (c.equals(Long.class)) {
            return ((Long) o).longValue();
        } else if (c.equals(Byte.class)) {
            return ((Byte) o).byteValue();
        } else if (c.equals(Short.class)) {
            return ((Short) o).shortValue();
        } else if (c.equals(Character.class)) {
            return ((Character) o).charValue();
        }

        return o;
    }

    private static Object numericValue(Object o, Class<?> c) {
        if (o == null) {
            return null;
        }

        if (c.equals(Integer.class)) {
            return Integer.parseInt(o.toString());
        } else if (c.equals(Float.class)) {
            return Float.parseFloat(o.toString());
        } else if (c.equals(Double.class)) {
            return Double.parseDouble(o.toString());
        } else if (c.equals(Long.class)) {
            return Long.parseLong(o.toString());
        } else if (c.equals(Byte.class)) {
            return Byte.parseByte(o.toString());
        } else if (c.equals(Short.class)) {
            return Short.parseShort(o.toString());
        }

        return o;
    }

    private List<Object> parseToSuitableArguments(Method method) throws IncompatibleArgumentsException {
        List<Object> suitableArguments = new ArrayList<Object>(arguments.size());

        if (arguments.size() != method.getParameterTypes().length) {
            throw new IncompatibleArgumentsException("Unequal arity. '" + arguments.size() + "' - '" + method.getParameterTypes().length + "'");
        }

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Object argument = arguments.get(i);
            Class<?> parameterType = method.getParameterTypes()[i];
            suitableArguments.add(parseToSuitableArgument(argument, parameterType));
        }

        return suitableArguments;
    }

    private static Object parseToSuitableArgument(Object argument, Class<?> parameterType)
            throws IncompatibleArgumentsException {
        if (argument == null) {
            if (!parameterType.isPrimitive()) {
                return argument;
            } else {
                throw new IncompatibleArgumentsException("Null is incompatible with primitive '" + parameterType + "'");
            }
        }

        if (argument.getClass() == parameterType) {
            return argument;
        } else if (mapToPrimitiveClass(argument.getClass()) == parameterType) {
            return primitiveValue(argument);
        }

        if (parameterType.isAssignableFrom(argument.getClass())) {
            return parameterType.cast(argument);
        }

        // Accept any number
        if (Number.class.isAssignableFrom(mapToBoxingClass(parameterType)) && !(argument instanceof CharSequence)) {
            Object value;

            try {
                value = numericValue(argument, mapToBoxingClass(parameterType));
            } catch (NumberFormatException e) {
                throw new IncompatibleArgumentsException("Cannot convert '" + argument + "' to class '" + parameterType + "'");
            }

            if (parameterType.isPrimitive()) {
                return primitiveValue(value);
            } else {
                return value;
            }
        }

        if (!parameterType.isPrimitive() && !argument.getClass().isPrimitive()
                && CharSequence.class.isAssignableFrom(parameterType)) {
            if (argument instanceof CharSequence) {
                return (CharSequence) argument;
            }
        }

        if (argument instanceof String) {
            if (((String) argument).length() == 1) {
                if (parameterType.equals(char.class)) {
                    return ((String) argument).charAt(0);
                } else if (parameterType.equals(Character.class)) {
                    return new Character(((String) argument).charAt(0));
                }
            }
        }

        throw new IncompatibleArgumentsException("No suitable type '" + parameterType + "' for '" + argument + "'");
    }

    private static Class<?>[] parseArgumentTypes(List<?> arguments) {
        Class<?>[] types = new Class<?>[arguments.size()];

        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i) == null) {
                types[i] = Object.class;
            } else {
                types[i] = arguments.get(i).getClass();
            }
        }

        return types;
    }

    public String getName() {
        return "InvocationOp[" + this.methodName + ", arguments = " + this.arguments + "]";
    }

    private static class IncompatibleArgumentsException extends Exception {
        public IncompatibleArgumentsException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class MethodWithArguments {
        private Method method;
        private List<Object> arguments;

        public MethodWithArguments(Method method, List<Object> arguments) {
            this.method = method;
            this.arguments = arguments;
        }

        public Method getMethod() {
            return method;
        }

        public List<Object> getArguments() {
            return arguments;
        }

        public Object invoke(Object o) throws InvocationTargetException, IllegalAccessException {
            int size = arguments.size();

            switch (size) {
                case 0:
                    return method.invoke(o);
                case 1:
                    return method.invoke(o, arguments.get(0));
                case 2:
                    return method.invoke(o, arguments.get(0), arguments.get(1));
                case 3:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2));
                case 4:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3));
                case 5:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4));
                case 6:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5));
                case 7:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6));
                case 8:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7));
                case 9:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8));
                case 10:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9));
                case 11:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10));
                case 12:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10), arguments.get(11));
                case 13:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10), arguments.get(11), arguments.get(12));
                case 14:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10), arguments.get(11), arguments.get(12), arguments.get(13));
                case 15:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10), arguments.get(11), arguments.get(12), arguments.get(13), arguments.get(14));
                case 16:
                    return method.invoke(o, arguments.get(0), arguments.get(1), arguments.get(2), arguments.get(3), arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7), arguments.get(8), arguments.get(9), arguments.get(10), arguments.get(11), arguments.get(12), arguments.get(13), arguments.get(14), arguments.get(15));
            }

            throw new UnsupportedOperationException("Method with more than 16 arguments are not supported");
        }
    }
}
