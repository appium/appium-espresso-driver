package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.KeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public interface W3CActionAdapter {

    boolean keyDown(KeyEvent keyDownEvent) throws AppiumException;

    boolean keyUp(KeyEvent keyUpEvent) throws AppiumException;

    void lockAdapter();

    void unlockAdapter();

    int getKeyCode(String keyValue, int location);

    int getCharCode(String keyValue, int location);

    int getWhich(String keyValue, int location);
    double getPointerMoveDurationMargin(PointerInputState pointerInputState);

    boolean pointerMoveEvent(String sourceId, InputSource.PointerType pointerType,
                             int currentX, int currentY, int x, int y,
                             Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException;

    int pointerMoveIntervalDuration();
}
