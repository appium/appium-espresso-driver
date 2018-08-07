package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.concurrent.locks.ReentrantLock;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

public abstract class BaseW3CActionAdapter implements W3CActionAdapter {

    private static ReentrantLock reentrantLock = new ReentrantLock();

    public int getKeyCode(String keyValue, int location) throws AppiumException {
        return Character.getNumericValue(keyValue.charAt(0));
    }

    public int getCharCode(String keyValue, int location) {
        return -1;
    }

    public int getWhich(String keyValue, int location) {
        return -1;
    }

    public void lockAdapter() {
        reentrantLock.lock();
    }

    public void unlockAdapter() {
        reentrantLock.unlock();
    }

    /**
     * Determines how close coordinates must be to the target coordinates before we
     * decide to just skip ahead to the target coordinates. 0.01 is arbitrary.
     *
     * (see item 4 of 'perform a pointer move' algorithm in 17.4.3)
     * @param pointerInputState What the state of the pointer is currently
     * @return How close the coordinates need to be to just go to the final coordinate.
     */
    public double getPointerMoveDurationMargin(PointerInputState pointerInputState) {
        return 0.01;
    }

    /**
     * How long (in ms) does the adapter need to perform a pointer move event
     *
     * (see 17.4.3)
     * @return Time in MS to perform operations
     */
    public int pointerMoveIntervalDuration() {
        // Default to 5 ms (120 moves per second)
        return 5;
    }

    public void sleep(long duration) throws AppiumException {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ie) {
            throw new AppiumException(String.format("Could not run 'sleep' method: %s", ie.getCause()));
        }
    }

    /**
     * Waits for the UI to complete before moving forward
     */
    public void waitForUiThread() {
        // No-op by default.
    }

    public void sychronousTickActionsComplete() throws AppiumException {
        // No-op by default
    }
}
