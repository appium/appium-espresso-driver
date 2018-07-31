package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.MotionEvent;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;

public class MotionEventBuilder {

    private MotionEventParams params;
    private final UiController uiController;

    public MotionEventBuilder(UiController uiController) {
        this.uiController = uiController;
        params = new MotionEventParams();
    }

    public MotionEventBuilder setX(long x) {
        params.x = x;
        return this;
    }

    public MotionEventBuilder setY(long y) {
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
        params.downTime = buttonState;
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

    public void run () throws AppiumException {
        MotionEvent evt = MotionEvent.obtain(
                params.downTime,
                SystemClock.uptimeMillis(),
                params.action,
                params.x,
                params.y,
                params.pressure,
                params.size,
                params.metaState,
                params.xPrecision,
                params.yPrecision,
                params.deviceId,
                params.edgeFlags
        );

        try {
            uiController.injectMotionEvent(evt);
        } catch (InjectEventSecurityException e) {
            throw new AppiumException(String.format(
                    "Could not complete pointer operation. An internal server error occurred: %s",
                    e.getCause()
            ));
        }
    }

    static class MotionEventParams {
        long downTime;
        int action;
        float x;
        float y;
        float pressure;
        float size;
        int metaState;
        float xPrecision;
        float yPrecision;
        int deviceId;
        int edgeFlags;
    }
}
