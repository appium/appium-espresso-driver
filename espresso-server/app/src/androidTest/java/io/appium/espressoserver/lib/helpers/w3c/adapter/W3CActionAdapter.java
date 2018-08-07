package io.appium.espressoserver.lib.helpers.w3c.adapter;

import android.graphics.Point;

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

public interface W3CActionAdapter {

    void keyDown(W3CKeyEvent keyDownEvent) throws AppiumException;

    void keyUp(W3CKeyEvent keyUpEvent) throws AppiumException;

    void pointerDown(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException;

    void pointerUp(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException;

    void pointerMove(String sourceId, PointerType pointerType,
                     long currentX, long currentY, long x, long y,
                     Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException;

    void pointerCancel(String sourceId, PointerType pointerType) throws AppiumException;

    void lockAdapter();

    void unlockAdapter();

    int getKeyCode(String keyValue, int location) throws AppiumException;

    int getCharCode(String keyValue, int location) throws AppiumException;

    int getWhich(String keyValue, int location) throws AppiumException;
    
    double getPointerMoveDurationMargin(PointerInputState pointerInputState);

    int pointerMoveIntervalDuration();

    void sleep(long duration) throws AppiumException;

    void waitForUiThread();

    Point getElementCenterPoint(String elementId) throws NoSuchElementException, StaleElementException, NotYetImplementedException;

    long getViewportWidth();

    long getViewportHeight();

    Logger getLogger();

    void sychronousTickActionsComplete() throws AppiumException;
}
