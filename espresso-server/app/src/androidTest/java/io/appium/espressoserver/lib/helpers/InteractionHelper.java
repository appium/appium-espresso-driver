package io.appium.espressoserver.lib.helpers;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.InputEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

public class InteractionHelper {
    private static UiDevice uiDevice;

    private static Method injectInputEventMethod;
    private static Object instanceInputManagerObject;
    private static int eventMode;

    private final static Lock eventsInjectorGuard = new ReentrantLock();

    private static void initEventsInjector() throws AppiumException {
        eventsInjectorGuard.lock();
        try {
            if (injectInputEventMethod != null) {
                return;
            }
            // This is how Espresso itself does the trickery:
            // https://android.googlesource.com/platform/frameworks/testing/+/android-support-test/espresso/core/src/main/java/android/support/test/espresso/base/InputManagerEventInjectionStrategy.java
            Class<?> inputManagerClassObject = Class.forName("android.hardware.input.InputManager");
            Method getInstanceMethod = inputManagerClassObject.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            instanceInputManagerObject = getInstanceMethod.invoke(inputManagerClassObject);
            injectInputEventMethod = instanceInputManagerObject.getClass()
                    .getDeclaredMethod("injectInputEvent", InputEvent.class, Integer.TYPE);
            injectInputEventMethod.setAccessible(true);
            Field motionEventModeField =
                    inputManagerClassObject.getField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");
            motionEventModeField.setAccessible(true);
            eventMode = motionEventModeField.getInt(inputManagerClassObject);
        } catch (Exception e) {
            throw new AppiumException(e);
        } finally {
            eventsInjectorGuard.unlock();
        }
    }

    public static synchronized UiDevice getUiDevice() {
        if (uiDevice == null) {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
        return uiDevice;
    }

    public static boolean injectEventSync(InputEvent event) throws AppiumException {
        initEventsInjector();
        try {
            return (Boolean) injectInputEventMethod.invoke(instanceInputManagerObject,
                    event, eventMode);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AppiumException(e);
        }
    }
}
