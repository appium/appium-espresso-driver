package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public class DummyW3CActionAdapter extends BaseW3CActionAdapter {

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

    private List<PointerMoveEvent> pointerMoveEvents = new ArrayList<>();

    public void keyDown(KeyDispatch.KeyEvent keyEvent) {

    }

    public void keyUp(KeyDispatch.KeyEvent keyEvent) {

    }

    public void pointerUp(int button, String sourceId, PointerType pointerType,
                            Long x, Long y, Set<Integer> depressedButtons,
                            KeyInputState globalKeyInputState) throws AppiumException {

    }

    public void pointerDown(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException {

    }

    public double getPointerMoveDurationMargin(PointerInputState pointerInputState) {
        if (pointerInputState.getType() == PointerType.TOUCH) {
            if (!pointerInputState.hasPressedButtons()) {
                // If no buttons are pushed nothing happens, so skip to the end
                // of the pointer move
                // ie: touch move without pressed buttons is like a finger movoing without
                //     being pressed on the screen
                return 1.0;
            }
        }

        // Give a margin of error of 1%
        return 0.01;
    }

    public void pointerMove(String sourceId, PointerType pointerType,
                            long currentX, long currentY, long x, long y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
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

    public long[] getElementCenterPoint(String elementId)
            throws NoSuchElementException, StaleElementException, NotYetImplementedException {
        if (elementId.equals("none")) {
            throw new NoSuchElementException(String.format("Could not find element with id: %s", elementId));
        } else if (elementId.equals("stale")) {
            throw new StaleElementException(String.format("Element with id %s no longer exists", elementId));
        }

        return new long[]  { 10L, 10L };
    }

    public long getViewportHeight() {
        return 400;
    }

    public long getViewportWidth() {
        return 200;
    }
}
