package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public abstract class BaseW3CActionAdapter implements W3CActionAdapter {

    private static ReentrantLock reentrantLock = new ReentrantLock();

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

    /**
     * Determines how close coordinates must be to the target coordinates before we
     * decide to just skip ahead to the target coordinates. 0.01 is arbitrary.
     * @param pointerInputState
     * @return
     */
    public double getPointerMoveDurationMargin(PointerInputState pointerInputState) {
        return 0.01;
    }

    /**
     * How long (in ms) does the adapter need to perform a pointer move event
     * @return
     */
    public int pointerMoveIntervalDuration() {
        // Default to ~15 moves-per-second
        return 67;
    }

    public void sleep(long duration) throws InterruptedException {
        Thread.sleep(duration);
    }
}
