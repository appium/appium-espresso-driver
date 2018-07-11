package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.Build;
import android.view.MotionEvent;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsConstants;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;
import static android.view.MotionEvent.TOOL_TYPE_MOUSE;
import static android.view.MotionEvent.TOOL_TYPE_STYLUS;

public class Helpers {

    private static final int MOUSE_BUTTON_LEFT = 0;
    private static final int MOUSE_BUTTON_MIDDLE = 1;
    private static final int MOUSE_BUTTON_RIGHT = 2;

    public static int extractButton(final Integer w3cButton, final PointerType pointerType)
            throws AppiumException {

        // Get the Android tool type constant associated with the W3C provided pointer type
        final int toolType;
        switch (pointerType) {
            case TOUCH:
                toolType = TOOL_TYPE_FINGER;
                break;
            case PEN:
                toolType = TOOL_TYPE_STYLUS;
                break;
            case MOUSE:
                toolType  = TOOL_TYPE_MOUSE;
                break;
            default:
                throw new AppiumException(String.format("Invalid tool type: %s", pointerType));
        }


        if (toolType == TOOL_TYPE_FINGER) {
            // Ignore button code conversion for the unsupported tool type
            if (w3cButton != null) {
                return w3cButton;
            }
            return 0;
        }

        int androidButton = ActionsConstants.MOUSE_BUTTON_LEFT;
        if (w3cButton != null) {
            androidButton = w3cButton;
        }
        // W3C button codes are different from Android constants. Converting...
        switch (androidButton) {
            case MOUSE_BUTTON_LEFT:
                if (toolType == MotionEvent.TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) {
                    return MotionEvent.BUTTON_STYLUS_PRIMARY;
                }
                return MotionEvent.BUTTON_PRIMARY;
            case MOUSE_BUTTON_MIDDLE:
                return MotionEvent.BUTTON_TERTIARY;
            case MOUSE_BUTTON_RIGHT:
                if (toolType == MotionEvent.TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) {
                    return MotionEvent.BUTTON_STYLUS_SECONDARY;
                }
                return MotionEvent.BUTTON_SECONDARY;
            default:
                return androidButton;
        }
    }

}
