package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.support.test.espresso.action.MotionEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.BaseW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

public class EspressoW3CActionAdapter extends BaseW3CActionAdapter {

    private final UiController uiController;
    private final Map<String, PointerDevice> pointerDevices = new HashMap<>();

    public EspressoW3CActionAdapter(UiController uiController) {
        this.uiController = uiController;
    }

    public void keyDown(KeyEvent keyEvent) throws AppiumException {
        // Stub.
        /*android.view.KeyEvent androidKeyEvent = new android.view.KeyEvent(
                System.currentTimeMillis(), System.currentTimeMillis(),
                ACTION_DOWN, Character.getNumericValue(keyEvent.getKey().charAt(0)),
                0
        );

        try {
            uiController.injectKeyEvent(androidKeyEvent);
        } catch (InjectEventSecurityException e) {
            throw new AppiumException(e.getCause().toString());
        }*/
    }

    public void keyUp(KeyEvent keyEvent) {
        // Stub.
    }

    public void pointerDown(int button, String sourceId, InputSource.PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer down at coordinates: %s %s", x, y));
        AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                .pointerDown(x, y, button, pointerType, globalKeyInputState);
    }

    public void pointerUp(int button, String sourceId, InputSource.PointerType pointerType,
                   Long x, Long y, Set<Integer> depressedButtons,
                   KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer up at coordinates: %s %s", x, y));
        AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                .pointerUp(x, y, button, pointerType, globalKeyInputState);
    }

    public void pointerMove(String sourceId, InputSource.PointerType pointerType,
                            long currentX, long currentY, long x, long y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
        // TODO: This should probably be
        this.getLogger().info(String.format("Running pointer move at coordinates: %s %s", x, y));
        AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                .pointerMove(x, y, globalKeyInputState);
    }

    public void pointerCancel(String sourceId, InputSource.PointerType pointerType) throws AppiumException {
        // Stub.
    }

    public long getViewportHeight() {
        // Stub.
        return Long.MAX_VALUE;
    }

    public long getViewportWidth() {
        // Stub.
        return Long.MAX_VALUE;
    }

    public long[] getElementCenterPoint(String elementId)
            throws NoSuchElementException, StaleElementException, NotYetImplementedException {
        // Stub.
        return new long[] { };
    }

    public void waitForUiThread() {
        uiController.loopMainThreadUntilIdle();
    }

    public void sleep(long duration) throws AppiumException {
        SystemClock.sleep(duration);
    }
    
    public Logger getLogger() {
        return AndroidLogger.logger;
    }
}
