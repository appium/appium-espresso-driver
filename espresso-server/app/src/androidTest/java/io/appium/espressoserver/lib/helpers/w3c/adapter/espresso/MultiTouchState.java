package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MultiTouchState.TouchPhase.DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MultiTouchState.TouchPhase.NONE;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MultiTouchState.TouchPhase.UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class MultiTouchState {

    private final Map<String, TouchState> touchStateSet = new LinkedHashMap<>();
    private KeyInputState globalKeyInputState;
    private int button;
    private MotionEvent downEvent;
    private TouchPhase touchPhase;

    public void updateTouchState(final int actionType,
                                 final String sourceId,
                                 final Long x, final Long y,
                                 final KeyInputState globalKeyInputState,
                                 final @Nullable Integer button) {

        // Lazily get the touch state of the input with given sourceId
        if (!touchStateSet.containsKey(sourceId)) {
            TouchState touchState = new TouchState();
            touchStateSet.put(sourceId, touchState);
        }

        // Update x and y coordinates
        TouchState touchState = touchStateSet.get(sourceId);
        touchState.setX(x);
        touchState.setY(y);

        // Update to global key input state
        this.globalKeyInputState = globalKeyInputState;
        if (button != null) {
            this.button = button;
        }

        // Record if we're in the TOUCH_DOWN or TOUCH_UP phase
        if (actionType == ACTION_DOWN) {
            touchPhase = DOWN;
        } else if (actionType == ACTION_UP) {
            touchPhase = UP;
        }
    }

    /**
     * Get the x coordinates for all inputs in the same order they were entered
     * @return X coordinates as a list
     */
    private List<Long> getXCoords() {
        List<Long> xCoords = new ArrayList<>();

        for(Map.Entry<String, TouchState> entry:touchStateSet.entrySet()) {
            xCoords.add(entry.getValue().getX());
        }

        return xCoords;
    }

    /**
     * Get the y coordinates for all inputs in the same order they were entered
     * @return Y coordinates as a list
     */
    private List<Long> getYCoords() {
        List<Long> yCoords = new ArrayList<>(touchStateSet.size());

        for(Map.Entry<String, TouchState> entry:touchStateSet.entrySet()) {
            yCoords.add(entry.getValue().getY());
        }

        return yCoords;
    }

    public void pointerDown(UiController uiController) throws AppiumException {
        AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getTouchMotionEvent(uiController);
        Long eventTime = SystemClock.uptimeMillis();
        List<Long> xCoords = getXCoords();
        List<Long> yCoords = getYCoords();
        this.downEvent = androidMotionEvent.pointerEvent(
                xCoords, yCoords,
                ACTION_DOWN, this.button, TOUCH, this.globalKeyInputState, null, eventTime);

        if (xCoords.size() > 1) {
            androidMotionEvent.pointerEvent(xCoords, yCoords,
                    ACTION_POINTER_DOWN, this.button, TOUCH, this.globalKeyInputState, downEvent, eventTime);
        }

    }

    public void pointerUp(UiController uiController) throws AppiumException {
        AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getTouchMotionEvent(uiController);
        Long eventTime = SystemClock.uptimeMillis();
        List<Long> xCoords = getXCoords();
        List<Long> yCoords = getYCoords();
        if (xCoords.size() > 1) {
            androidMotionEvent.pointerEvent(
                    xCoords, yCoords,
                    ACTION_POINTER_UP, this.button, TOUCH, this.globalKeyInputState, downEvent, eventTime);
        }

        androidMotionEvent.pointerEvent(
                xCoords, yCoords,
                ACTION_UP, this.button, TOUCH, this.globalKeyInputState, downEvent, eventTime);

        this.downEvent = null;
    }

    public void pointerCancel(UiController uiController) throws AppiumException {
        List<Long> xCoords = getXCoords();
        List<Long> yCoords = getYCoords();
        AndroidMotionEvent.getTouchMotionEvent(uiController).pointerEvent(
                xCoords, yCoords,
                ACTION_CANCEL, this.button, TOUCH, this.globalKeyInputState, downEvent, SystemClock.uptimeMillis()
        );
        this.downEvent = null;
        touchPhase = NONE;
    }

    public void pointerMove(UiController uiController) throws AppiumException {
        if (this.isDown()) {
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getTouchMotionEvent(uiController);
            androidMotionEvent.pointerMove(
                    getXCoords(), getYCoords(),
                    TOUCH, globalKeyInputState, downEvent);
        }
    }

    public boolean isDown () {
        return downEvent != null;
    }

    public void perform(UiController uiController) throws AppiumException {
        if (touchPhase == DOWN) {
            pointerDown(uiController);
        } else if (touchPhase == UP) {
            pointerUp(uiController);
        }
        touchPhase = NONE;
    }

    public enum TouchPhase {
        DOWN,
        UP,
        NONE
    }
}
