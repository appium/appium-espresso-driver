package io.appium.espressoserver.lib.helpers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import androidx.test.platform.app.InstrumentationRegistry;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static io.appium.espressoserver.lib.helpers.AndroidLogger.logger;

//https://github.com/calabash/calabash-android-server/blob/develop/server/app/src/androidTest/java/sh/calaba/instrumentationbackend/query/InvocationOperation.java

public class InvocationOperation {
    public static final String VOID = "<VOID>";
    private final String methodName;
    private final Class<?>[] argumentTypes;
    private final Object[] argumentValues;

    public InvocationOperation(String methodName, Object[] argumentValues, Class<?>[] argumentTypes) {
        this.methodName = methodName;
        this.argumentValues = argumentValues;
        this.argumentTypes = argumentTypes;
    }


    public Object apply(final Object applyOn) throws Exception {
        logger.debug(String.format("Backdoor Method: %s", methodName));
        logger.debug(String.format("Invoking on: %s", applyOn.toString()));

        final AtomicReference<Object> ref = new AtomicReference<Object>();
        final AtomicReference<Exception> refEx = new AtomicReference<Exception>();
        final Method method = findCompatibleMethod(applyOn);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Object result;
                try {
                    result = ReflectionUtils.invoke(method, applyOn, argumentValues);
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

    public Method findCompatibleMethod(Object object) throws AppiumException {
        try {
            return object.getClass().getMethod(methodName, argumentTypes);
        } catch (NoSuchMethodException e) {
            throw new AppiumException(String.format
                    ("No method %s definded on %s which takes argument %s", methodName, object.getClass(), Arrays.toString(argumentTypes))
                    , e);
        }
    }
}
