package io.appium.espressoserver.lib.helpers.w3c.adapter;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public class DummyW3CActionAdapter extends BaseW3CActionAdapter {

    // Keep a log of pointer move events so the values can be checked in the unit tests
    private List<PointerMoveEvent> pointerMoveEvents = new ArrayList<>();

    public static class PointerMoveEvent {
        public String sourceId;
        public PointerType pointerType;
        public long currentX;
        public long currentY;
        public long x;
        public long y;
        public Set<Integer> buttons;
        public KeyInputState globalKeyInputState;
    }

    private class DummyLogger implements Logger {
        public void error(Object... messages) {
            // No-op
        }
        public void error(String message, Throwable throwable) {
            // No-op
        }
        public void info(Object... messages) {
            // No-op
        }
        public void debug(Object... messages) {
            // No-op
        }
    }

    private Logger logger = new DummyLogger();

    public void keyDown(W3CKeyEvent keyEvent) {
        // No-op
    }

    public void keyUp(W3CKeyEvent keyEvent) {
        // No-op
    }

    public void pointerUp(int button, String sourceId, PointerType pointerType,
                            Long x, Long y, Set<Integer> depressedButtons,
                            KeyInputState globalKeyInputState) throws AppiumException {
        // No-op
    }

    public void pointerDown(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException {
        // No-op
    }

    public void pointerCancel(String sourceId, PointerType pointerType) throws AppiumException {
        // No-op
    }

    public double getPointerMoveDurationMargin(PointerInputState pointerInputState) {
        if (pointerInputState.getType() == PointerType.TOUCH && !pointerInputState.hasPressedButtons()) {
            // If no buttons are pushed nothing happens, so skip to the end
            // of the pointer move
            // e.g.: touch move without pressed buttons is like a finger moving without
            //      being pressed on the screen
            return 1.0;
        }

        // Give a margin of error of 1%
        return 0.01;
    }

    public void pointerMove(String sourceId, PointerType pointerType,
                            long currentX, long currentY, long x, long y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
        // Add the pointer move event to the logs
        PointerMoveEvent pointerMoveEvent = new PointerMoveEvent();
        pointerMoveEvent.sourceId = sourceId;
        pointerMoveEvent.pointerType = pointerType;
        pointerMoveEvent.currentX = currentX;
        pointerMoveEvent.currentY = currentY;
        pointerMoveEvent.x = x;
        pointerMoveEvent.y = y;
        pointerMoveEvent.buttons = buttons;
        pointerMoveEvent.globalKeyInputState = globalKeyInputState;
        pointerMoveEvents.add(pointerMoveEvent);
    }

    public List<PointerMoveEvent> getPointerMoveEvents() {
        return pointerMoveEvents;
    }

    public Point getElementCenterPoint(String elementId)
            throws NoSuchElementException, StaleElementException, NotYetImplementedException {
        if ("none".equals(elementId)) {
            throw new NoSuchElementException(String.format("Could not find element with id: %s", elementId));
        } else if ("stale".equals(elementId)) {
            throw new StaleElementException(String.format("Element with id %s no longer exists", elementId));
        }

        Point point = new Point();
        point.x = 10;
        point.y = 10;
        return point;
    }

    public long getViewportHeight() {
        return 400;
    }

    public long getViewportWidth() {
        return 200;
    }

    public Logger getLogger() {
        return logger;
    }
}
