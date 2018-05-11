package io.appium.espressoserver.lib.helpers;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.InputEvent;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

import static io.appium.espressoserver.lib.helpers.ReflectionUtils.getField;
import static io.appium.espressoserver.lib.helpers.ReflectionUtils.invoke;
import static io.appium.espressoserver.lib.helpers.ReflectionUtils.method;

public class InteractionHelper {
    private static final String CLASS_UI_AUTOMATOR_BRIDGE =
            "android.support.test.uiautomator.UiAutomatorBridge";
    private static final String METHOD_INJECT_INPUT_EVENT = "injectInputEvent";
    private static final String FIELD_UI_AUTOMATOR_BRIDGE = "mUiAutomationBridge";

    private static Object uiAutomatorBridge;
    private static UiDevice uiDevice;

    public static UiDevice getUiDevice() {
        if (uiDevice == null) {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
        return uiDevice;
    }

    private static synchronized Object getUiAutomatorBridge() throws AppiumException {
        if (uiAutomatorBridge == null) {
            uiAutomatorBridge = getField(UiDevice.class, FIELD_UI_AUTOMATOR_BRIDGE, getUiDevice());
        }
        return uiAutomatorBridge;
    }

    public static boolean injectEventSync(InputEvent event) throws AppiumException {
        return (Boolean) invoke(method(CLASS_UI_AUTOMATOR_BRIDGE, METHOD_INJECT_INPUT_EVENT,
                InputEvent.class, boolean.class), getUiAutomatorBridge(), event, true);
    }
}
