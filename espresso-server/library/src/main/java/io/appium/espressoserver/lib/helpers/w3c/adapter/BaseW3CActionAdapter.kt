package io.appium.espressoserver.lib.helpers.w3c.adapter

import java.util.concurrent.locks.ReentrantLock

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

abstract class BaseW3CActionAdapter : W3CActionAdapter {

    override fun getKeyCode(keyValue: String?, location: Int): Int {
        keyValue?.let {
            return Character.getNumericValue(keyValue[0])
        }
        return -1
    }

    override fun getCharCode(keyValue: String?, location: Int): Int = -1

    override fun getWhich(keyValue: String?, location: Int): Int = -1

    override fun lockAdapter() {
        reentrantLock.lock()
    }

    override fun unlockAdapter() {
        reentrantLock.unlock()
    }

    /**
     * Determines how close coordinates must be to the target coordinates before we
     * decide to just skip ahead to the target coordinates. 0.01 is arbitrary.
     *
     * (see item 4 of 'perform a pointer move' algorithm in 17.4.3)
     * @param pointerInputState What the state of the pointer is currently
     * @return How close the coordinates need to be to just go to the final coordinate.
     */
    override fun getPointerMoveDurationMargin(pointerInputState: PointerInputState): Double = 0.01

    /**
     * How long (in ms) does the adapter need to perform a pointer move event
     *
     * (see 17.4.3)
     * @return Time in MS to perform operations
     */
    override fun pointerMoveIntervalDuration(): Int = 5 // Default to 5 ms (120 moves per second)

    @Throws(AppiumException::class)
    override fun sleep(duration: Float) {
        try {
            Thread.sleep(Math.round(duration).toLong())
        } catch (ie: InterruptedException) {
            throw AppiumException("Could not run 'sleep' method: ${ie.cause}")
        }
    }

    /**
     * Waits for the UI to complete before moving forward
     */
    override fun waitForUiThread() {
        // No-op by default.
    }

    @Throws(AppiumException::class)
    override fun sychronousTickActionsComplete() {
        // No-op by default
    }

    companion object {
        private val reentrantLock = ReentrantLock()
    }
}
