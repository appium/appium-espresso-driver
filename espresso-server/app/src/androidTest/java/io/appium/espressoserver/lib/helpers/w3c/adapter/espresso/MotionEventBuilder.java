package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;
import static android.view.MotionEvent.TOOL_TYPE_MOUSE;
import static android.view.MotionEvent.TOOL_TYPE_STYLUS;

public class MotionEventBuilder {

    private MotionEventParams params;
    private final UiController uiController;

    public MotionEventBuilder(UiController uiController) {
        this.uiController = uiController;
        params = new MotionEventParams();
    }

    public MotionEventBuilder setX(List<Long> x) {
        params.x = x;
        return this;
    }

    public MotionEventBuilder setY(List<Long> y) {
        params.y = y;
        return this;
    }

    public MotionEventBuilder setDownTime(long downTime) {
        params.downTime = downTime;
        return this;
    }

    public MotionEventBuilder setAction(int action) {
        params.action = action;
        return this;
    }

    public MotionEventBuilder setMetaState(int metaState) {
        params.metaState = metaState;
        return this;
    }

    public MotionEventBuilder setButtonState(int buttonState) {
        params.buttonState = buttonState;
        return this;
    }

    public MotionEventBuilder setXPrecision(float xPrecision) {
        params.xPrecision = xPrecision;
        return this;
    }

    public MotionEventBuilder setYPrecision(float yPrecision) {
        params.yPrecision = yPrecision;
        return this;
    }

    public MotionEventBuilder setDeviceId(int deviceId) {
        params.deviceId = deviceId;
        return this;
    }

    public MotionEventBuilder setEdgeFlags(int edgeFlags) {
        params.edgeFlags = edgeFlags;
        return this;
    }

    public MotionEventBuilder setPointerType(PointerType pointerType) {
        params.pointerType = pointerType;
        return this;
    }

    public void run () throws AppiumException {
        int pointerCount = params.x.size();
        PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
        PointerProperties[] pointerProperties = new PointerProperties[pointerCount];

        for (int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++) {
            pointerCoords[pointerIndex] = new PointerCoords();
            pointerCoords[pointerIndex].pressure = 1;
            pointerCoords[pointerIndex].size = 1;
            pointerCoords[pointerIndex].x = params.x.get(pointerIndex);
            pointerCoords[pointerIndex].y = params.y.get(pointerIndex);

            pointerProperties[pointerIndex] = new PointerProperties();
            pointerProperties[pointerIndex].toolType = getToolType(params.pointerType);
            pointerProperties[pointerIndex].id = pointerIndex;

        }

        MotionEvent evt = MotionEvent.obtain(
                params.downTime,
                SystemClock.uptimeMillis(),
                params.action,
                pointerCount,
                pointerProperties,
                pointerCoords,
                params.metaState,
                params.buttonState,
                params.xPrecision,
                params.yPrecision,
                params.deviceId,
                params.edgeFlags,
                0, // TODO: How to get source?
                0 // TODO: How to get Motion Event flags?
        );

        /*long downTime, long eventTime,
        int action, int pointerCount, PointerProperties[] pointerProperties,
                PointerCoords[] pointerCoords, int metaState, int buttonState,
        float xPrecision, float yPrecision, int deviceId,
        int edgeFlags, int source, int flags*/


        try {
            uiController.injectMotionEvent(evt);
        } catch (InjectEventSecurityException e) {
            throw new AppiumException(String.format(
                    "Could not complete pointer operation. An internal server error occurred: %s",
                    e.getCause()
            ));
        }
    }

    public int getToolType(PointerType pointerType) {
        if (pointerType == null) {
            return TOOL_TYPE_FINGER;
        }
        switch (pointerType) {
            case TOUCH:
                return TOOL_TYPE_FINGER;
            case PEN:
                return TOOL_TYPE_STYLUS;
            case MOUSE:
            default:
                return TOOL_TYPE_MOUSE;
        }
    }

    static class MotionEventParams {
        long downTime;
        int action;
        List<Long> x;
        List<Long> y;
        float pressure;
        float size;
        int metaState;
        float xPrecision;
        float yPrecision;
        int deviceId;
        int edgeFlags;
        int buttonState;
        PointerType pointerType;
    }
}
