package io.appium.espressoserver.lib.helpers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.test.platform.app.InstrumentationRegistry;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

//https://github.com/calabash/calabash-android-server/blob/develop/server/app/src/androidTest/java/sh/calaba/instrumentationbackend/query/InvocationOperation.java

public class InvocationOperation {
    public static final String VOID = "<VOID>";
    private final String methodName;
    private final Class<?>[] argumentTypes;
    private final Object[] argumentValues;
    private final Executor executor;

    public InvocationOperation(String methodName, Object[] argumentValues, Class<?>[] argumentTypes) {
        this(methodName, argumentValues, argumentTypes, new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
            }
        });
    }

    @VisibleForTesting
    public InvocationOperation(String methodName, Object[] argumentValues, Class<?>[] argumentTypes, Executor executor) {
        this.methodName = methodName;
        this.argumentValues = argumentValues;
        this.argumentTypes = argumentTypes;
        this.executor = executor;
    }


    public Object apply(final Object applyOn) throws Exception {
        final AtomicReference<Object> ref = new AtomicReference<>();
        final AtomicReference<Exception> refEx = new AtomicReference<>();
        final Method method = findCompatibleMethod(applyOn);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Object result;
                try {
                    result = method.invoke(applyOn, argumentValues);
                    if (method.getReturnType().equals(Void.TYPE)) {
                        result = VOID;
                    }

                    ref.set(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    refEx.set(e);
                }
            }
        });

        if (refEx.get() != null) {
            throw refEx.get();
        }

        return ref.get();
    }

    private Method findCompatibleMethod(Object target) throws AppiumException {
        try {
            Method result = target.getClass().getMethod(methodName, argumentTypes);
            result.setAccessible(true);
            return result;
        } catch (NoSuchMethodException e) {
            throw new AppiumException(String.format
                    ("No public method '%s' is defined on %s or its parents which takes %s arguments", methodName,
                            target.getClass(), Arrays.toString(argumentTypes)), e);
        }
    }
}
