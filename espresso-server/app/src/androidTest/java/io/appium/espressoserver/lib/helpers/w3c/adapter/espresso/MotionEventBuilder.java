package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_INDEX_SHIFT;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.getToolType;

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

    public MotionEventBuilder setEventTime(long eventTime) {
        params.eventTime = eventTime;
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

    public MotionEventBuilder setSource(int source) {
        params.source = source;
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

    public MotionEvent run () throws AppiumException {
        int pointerCount = params.x == null ? 0 : params.x.size();

        AndroidLogger.logger.info("Calling pointers", pointerCount);

        // Don't do anything if no pointers were provided
        if (pointerCount == 0 && params.action != ACTION_CANCEL) {
            return null;
        }

        PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
        PointerProperties[] pointerProperties = new PointerProperties[pointerCount];

        for (int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++) {
            // Set pointer coordinates
            pointerCoords[pointerIndex] = new PointerCoords();
            pointerCoords[pointerIndex].clear();
            pointerCoords[pointerIndex].pressure = 1;
            pointerCoords[pointerIndex].size = 1;
            pointerCoords[pointerIndex].x = params.x.get(pointerIndex);
            pointerCoords[pointerIndex].y = params.y.get(pointerIndex);

            // Set pointer properties
            pointerProperties[pointerIndex] = new PointerProperties();
            pointerProperties[pointerIndex].toolType = getToolType(params.pointerType);
            pointerProperties[pointerIndex].id = pointerIndex;

        }

        // ACTION_POINTER_DOWN and ACTION_POINTER_UP need a bit mask
        int action = params.action;
        if (pointerCount > 1 && (action == ACTION_POINTER_DOWN || action == ACTION_POINTER_UP)) {
            action += (pointerProperties[1].id << ACTION_POINTER_INDEX_SHIFT);
        }

        // ACTION_DOWN and ACTION_UP and ACTION_CANCEL has a pointer count of 1
        if (action == ACTION_DOWN || action == ACTION_UP || action == ACTION_CANCEL) {
            if (params.x != null && params.y != null) {
                pointerCount = 1;
            } else {
                pointerCount = 0;
            }
        }

        MotionEvent evt = MotionEvent.obtain(
                params.downTime,
                params.eventTime > 0 ? params.eventTime : SystemClock.uptimeMillis(),
                action,
                pointerCount,
                pointerProperties,
                pointerCoords,
                params.metaState,
                params.buttonState,
                params.xPrecision,
                params.yPrecision,
                params.deviceId,
                params.edgeFlags,
                params.source,
                0 // TODO: How to get Motion Event flags?
        );

        try {
            boolean success = uiController.injectMotionEvent(evt);
            if (!success) {
                throw new AppiumException(String.format(
                        "Could not complete pointer operation"
                ));
            }
        } catch (InjectEventSecurityException e) {
            throw new AppiumException(String.format(
                    "Could not complete pointer operation. An internal server error occurred: %s",
                    e.getCause()
            ));
        }

        return evt;
    }

    static class MotionEventParams {
        long downTime;
        int action;
        List<Long> x;
        List<Long> y;
        int metaState;
        float xPrecision;
        float yPrecision;
        int deviceId;
        int edgeFlags;
        int buttonState;
        int source;
        PointerType pointerType;
        long eventTime;
    }
}
