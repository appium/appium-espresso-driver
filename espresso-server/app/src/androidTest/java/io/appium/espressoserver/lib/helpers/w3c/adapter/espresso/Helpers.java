package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.graphics.Point;
import android.os.Build;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c_actions.ActionsConstants;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;
import static android.view.MotionEvent.TOOL_TYPE_MOUSE;
import static android.view.MotionEvent.TOOL_TYPE_STYLUS;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.MOUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class Helpers {

    private static final int MOUSE_BUTTON_LEFT = 0;
    private static final int MOUSE_BUTTON_MIDDLE = 1;
    private static final int MOUSE_BUTTON_RIGHT = 2;

    public static int extractButton(final Integer w3cButton, final PointerType pointerType)
            throws AppiumException {

        // Get the Android tool type constant associated with the W3C provided pointer type
        final int toolType = getToolType(pointerType);

        if (toolType == TOOL_TYPE_FINGER) {
            // Ignore button code conversion for the unsupported tool type
            return w3cButton != null ? w3cButton : 0;
        }

        int androidButton = ActionsConstants.MOUSE_BUTTON_LEFT;
        if (w3cButton != null) {
            androidButton = w3cButton;
        }
        // W3C button codes are different from Android constants. Converting...
        switch (androidButton) {
            case MOUSE_BUTTON_LEFT:
                return (toolType == TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) ?
                    MotionEvent.BUTTON_STYLUS_PRIMARY :
                    MotionEvent.BUTTON_PRIMARY;
            case MOUSE_BUTTON_MIDDLE:
                return MotionEvent.BUTTON_TERTIARY;
            case MOUSE_BUTTON_RIGHT:
                return (toolType == TOOL_TYPE_STYLUS && Build.VERSION.SDK_INT >= 23) ?
                    MotionEvent.BUTTON_STYLUS_SECONDARY :
                    MotionEvent.BUTTON_SECONDARY;
            default:
                return androidButton;
        }
    }

    /**
     * Return Android tool type based on W3C pointer type
     * @param pointerType W3C pointer types (TOUCH, PEN, MOUSE)
     * @return Android Motion Event type (FINGER, STYLUS, MOUSE)
     */
    public static int getToolType(@Nullable PointerType pointerType) throws AppiumException {
        if (pointerType == null) {
            return TOOL_TYPE_FINGER;
        }
        switch (pointerType) {
            case TOUCH:
                return TOOL_TYPE_FINGER;
            case PEN:
                return TOOL_TYPE_STYLUS;
            case MOUSE:
                return TOOL_TYPE_MOUSE;
            default:
                throw new AppiumException(String.format("Invalid tool type: %s", pointerType));
        }
    }

    // If a 'mouse' event was provided, convert it to 'touch'
    // This is because some clients only send 'mouse' events and the assumption is that if they
    // send 'mouse' events to a device that has a touch screen, it needs to be converted
    public static boolean isTouch(PointerType type) {
        // return type == TOUCH || (type == MOUSE && isTouchScreen); // TODO Revisit this if we wish to support MOUSE on Android
        return type == TOUCH || type == MOUSE;
    }

    /**
     * Convert [x,y] coordinates from float to long.
     *
     * Gives warning if the long values are different from the float values
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Rounded x and y coordinates
     */
    public static Point toCoordinates(Float x, Float y) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        if (x != roundedX || y != roundedY) {
            AndroidLogger.getLogger().warn(String.format("Coordinates provided [%s, %s] will be rounded to integers [%s %s]", x, y, roundedX, roundedY));
        }

        return new Point(roundedX, roundedY);
    }
}
