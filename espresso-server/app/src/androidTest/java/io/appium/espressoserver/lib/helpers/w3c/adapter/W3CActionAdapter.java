package io.appium.espressoserver.lib.helpers.w3c.adapter;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.KeyEvent;

public interface W3CActionAdapter {

    boolean keyDown(KeyEvent keyDownEvent) throws AppiumException;

    boolean keyUp(KeyEvent keyUpEvent) throws AppiumException;

    void lockAdapter();

    void unlockAdapter();
}
