package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.concurrent.locks.ReentrantLock;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;

public abstract class BaseW3CActionAdapter implements W3CActionAdapter {

    private static ReentrantLock reentrantLock = new ReentrantLock();

    public boolean keyDown(KeyDispatch.KeyEvent keyDownEvent) throws AppiumException {
        throw new NotYetImplementedException();
    }

    public boolean keyUp(KeyDispatch.KeyEvent keyUpEvent) throws AppiumException {
        throw new NotYetImplementedException();
    }

    public int getKeyCode(String keyValue, int location) {
        return Character.getNumericValue(keyValue.charAt(0));
    }

    public int getCharCode(String keyValue, int location) {
        return -1;
    }

    public int getWhich(String keyValue, int location) {
        return -1;
    }

    public synchronized void lockAdapter() {
        reentrantLock.lock();
    }

    public synchronized void unlockAdapter() {
        reentrantLock.unlock();
    }

}
