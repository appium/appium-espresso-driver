package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.KeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public interface W3CActionAdapter {

    void keyDown(KeyEvent keyDownEvent) throws AppiumException;

    void keyUp(KeyEvent keyUpEvent) throws AppiumException;

    void pointerDown(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException;

    void pointerUp(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException;

    void pointerMove(String sourceId, PointerType pointerType,
                     long currentX, long currentY, long x, long y,
                     Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException;

    void lockAdapter();

    void unlockAdapter();

    int getKeyCode(String keyValue, int location);

    int getCharCode(String keyValue, int location);

    int getWhich(String keyValue, int location);
    double getPointerMoveDurationMargin(PointerInputState pointerInputState);

    int pointerMoveIntervalDuration();

    void sleep(long duration) throws InterruptedException;

    long[] getElementCenterPoint(String elementId) throws NoSuchElementException, StaleElementException, NotYetImplementedException;

    long getViewportWidth();

    long getViewportHeight();
}
