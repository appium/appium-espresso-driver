package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public class DummyW3CActionAdapter extends BaseW3CActionAdapter {

    public static class PointerMoveEvent {
        public String sourceId;
        public PointerType pointerType;
        public int currentX;
        public int currentY;
        public int x;
        public int y;
        public Set<Integer> buttons;
        public KeyInputState globalKeyInputState;
    }

    private List<PointerMoveEvent> pointerMoveEvents = new ArrayList<>();

    public boolean keyDown(KeyDispatch.KeyEvent keyEvent) {
        return true;
    }

    public boolean keyUp(KeyDispatch.KeyEvent keyEvent) {
        return true;
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

    public boolean pointerMoveEvent(String sourceId, PointerType pointerType,
                                    int currentX, int currentY, int x, int y,
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
        return true;
    }


    public List<PointerMoveEvent> getPointerMoveEvents() {
        return pointerMoveEvents;
    }
}
