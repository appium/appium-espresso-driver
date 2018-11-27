package io.appium.espressoserver.lib.helpers;

import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import androidx.test.platform.app.InstrumentationRegistry;
import io.appium.espressoserver.lib.model.BackdoorResultVoid;
import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

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


    public Object apply(final Object applyOn) throws Exception {
        logger.debug(String.format("Backdoor Method: %s", methodName));
        logger.debug(String.format("Invoking on: %s", applyOn.toString()));

        final AtomicReference<Object> ref = new AtomicReference<Object>();
        final AtomicReference<Exception> refEx = new AtomicReference<Exception>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                MethodWithArguments method = findCompatibleMethod(applyOn);

                if (method == null) {
                    StringBuilder stringBuilder = new StringBuilder("No such method found: ");
                    stringBuilder.append(InvocationOperation.this.methodName);
                    stringBuilder.append("(");

                    for(Object argument: InvocationOperation.this.arguments){
                        String simpleName = argument == null ? "null" : argument.getClass().getSimpleName();;
                        stringBuilder.append("[").append(simpleName).append("]");
                    }
                    stringBuilder.append(")");

                    logger.debug("Failed to find a suitable method using reflection");
                    ref.set(BackdoorResultVoid.instance
                            .asMap(InvocationOperation.this.methodName, applyOn, stringBuilder.toString()));
                    return;
                }

                Object result;

                try {
                    if (method.getMethod().getReturnType().equals(Void.TYPE)) {
                        method.invoke(applyOn);
                        result = "<VOID>";
                    } else result = method.invoke(applyOn);

                    ref.set(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    refEx.set(e);
                }
            }
        });

        if (refEx.get() != null) throw refEx.get();

        return ref.get();
    }

    public MethodWithArguments findCompatibleMethod(Object object) {
        return findCompatibleMethod(object.getClass());
    }

    @Nullable
    public MethodWithArguments findCompatibleMethod(Class<?> forClass) {
        // Fast path
        try {
            return new MethodWithArguments(forClass.getMethod(methodName, argumentTypes),
                    new ArrayList<Object>(arguments));
        } catch (NoSuchMethodException e) {
            // No immediate method found

            for (Method method : forClass.getMethods()) {
                if (!method.getName().equals(methodName)) continue;

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

    private static Class<?> mapToBoxingClass(Class<?> c) {
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

    private static Object numericValue(Object o, Class<?> c) {
        if (o == null) return null;

        if (c.equals(Integer.class)) return Integer.parseInt(o.toString());
        if (c.equals(Float.class)) return Float.parseFloat(o.toString());
        if (c.equals(Double.class)) return Double.parseDouble(o.toString());
        if (c.equals(Long.class)) return Long.parseLong(o.toString());
        if (c.equals(Byte.class)) return Byte.parseByte(o.toString());
        if (c.equals(Short.class)) return Short.parseShort(o.toString());

        return o;
    }

    private List<Object> parseToSuitableArguments(Method method) throws IncompatibleArgumentsException {
        List<Object> suitableArguments = new ArrayList<Object>(arguments.size());

        if (arguments.size() != method.getParameterTypes().length)
            throw new IncompatibleArgumentsException(String.format("Unequal arity. '%d' - '%d'", arguments.size(), method.getParameterTypes().length));

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
            if (!parameterType.isPrimitive()) return null;
            throw new IncompatibleArgumentsException(String.format("Null is incompatible with primitive '%s'", parameterType));
        }

        if (argument.getClass() == parameterType) return argument;
        if (mapToPrimitiveClass(argument.getClass()) == parameterType)
            return argument;

        if (parameterType.isAssignableFrom(argument.getClass()))
            return parameterType.cast(argument);

        // Accept any number
        if (Number.class.isAssignableFrom(mapToBoxingClass(parameterType)) && !(argument instanceof CharSequence)) {
            Object value;

            try {
                value = numericValue(argument, mapToBoxingClass(parameterType));
            } catch (NumberFormatException e) {
                throw new IncompatibleArgumentsException(String.format("Cannot convert '%s' to class '%s'", argument, parameterType));
            }

//            if (parameterType.isPrimitive()) return primitiveValue(value);
            return value;
        }

        if (!parameterType.isPrimitive() && !argument.getClass().isPrimitive()
                && CharSequence.class.isAssignableFrom(parameterType))
            if (argument instanceof CharSequence) return (CharSequence) argument;

        if (argument instanceof String) if (((String) argument).length() == 1) {
            if (parameterType.equals(char.class)) return ((String) argument).charAt(0);
            if (parameterType.equals(Character.class))
                return ((String) argument).charAt(0);
        }

        throw new IncompatibleArgumentsException(String.format("No suitable type '%s' for '%s'", parameterType, argument));
    }

    private static Class<?>[] parseArgumentTypes(List<?> arguments) {
        Class<?>[] types = new Class<?>[arguments.size()];

        for (int i = 0; i < arguments.size(); i++)
            if (arguments.get(i) == null) types[i] = Object.class;
            else types[i] = arguments.get(i).getClass();

        return types;
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
