package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.support.test.espresso.UiController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class MultiTouchState {

    private Map<String, TouchState> touchStateSet = new LinkedHashMap<>();

    public void updateTouchState(String sourceId, Long x, Long y) {
        if (!touchStateSet.containsKey(sourceId)) {
            TouchState touchState = new TouchState();
            touchStateSet.put(sourceId, touchState);
        }
        TouchState touchState = touchStateSet.get(sourceId);
        touchState.setX(x);
        touchState.setY(y);
    }

    public List<Long> getXCoords() {
        List<Long> xCoords = new ArrayList<>();

        for(Map.Entry<String, TouchState> entry:touchStateSet.entrySet()) {
            xCoords.add(entry.getValue().getX());
        }

        return xCoords;
    }

    public List<Long> getYCoords() {
        List<Long> yCoords = new ArrayList<>(touchStateSet.size());

        for(Map.Entry<String, TouchState> entry:touchStateSet.entrySet()) {
            yCoords.add(entry.getValue().getY());
        }

        return yCoords;
    }

    public void pointerDown(String sourceId, UiController uiController) throws AppiumException {
        TouchState touchState = touchStateSet.get(sourceId);
        AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
        androidMotionEvent.pointerUpOrDown(
                getXCoords(), getYCoords(),
                ACTION_DOWN, touchState.getButton(), TOUCH, touchState.getGlobalKeyInputState());

        androidMotionEvent.pointerUpOrDown(getXCoords(), getYCoords(),
                ACTION_POINTER_DOWN, touchState.getButton(), TOUCH, touchState.getGlobalKeyInputState());

    }

    public void pointerUp(String sourceId, UiController uiController) throws AppiumException {
        TouchState touchState = touchStateSet.get(sourceId);
        AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
        androidMotionEvent.pointerUpOrDown(
                getXCoords(), getYCoords(),
                ACTION_POINTER_UP, touchState.getButton(), TOUCH, touchState.getGlobalKeyInputState());

        androidMotionEvent.pointerUpOrDown(getXCoords(), getYCoords(),
                ACTION_UP, touchState.getButton(), TOUCH, touchState.getGlobalKeyInputState());

    }

    public void pointerMove(String sourceId, UiController uiController) throws AppiumException {
        TouchState touchState = touchStateSet.get(sourceId);
        AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
        androidMotionEvent.pointerUpOrDown(
                getXCoords(), getYCoords(),
                ACTION_MOVE, touchState.getButton(), TOUCH, touchState.getGlobalKeyInputState());
    }
}
