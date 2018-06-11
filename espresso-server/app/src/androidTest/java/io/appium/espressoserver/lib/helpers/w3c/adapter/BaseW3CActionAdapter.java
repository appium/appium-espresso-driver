package io.appium.espressoserver.lib.helpers.w3c.adapter;

import java.util.concurrent.locks.ReentrantLock;

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

}
