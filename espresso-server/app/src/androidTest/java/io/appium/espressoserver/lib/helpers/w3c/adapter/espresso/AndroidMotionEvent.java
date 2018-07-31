package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.UiController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.extractButton;

public class AndroidMotionEvent {

    private long downTime;
    private final UiController uiController;
    private static final Map<String, AndroidMotionEvent> motionEvents = new ConcurrentHashMap<>();

    private AndroidMotionEvent(UiController uiController) {
        this.downTime = SystemClock.uptimeMillis();
        this.uiController = uiController;

        // TODO: How do we distinguish STYLUS, MOUSE and TOUCH?
    }

    public void pointerDown(long x, long y,
                            Integer button, PointerType pointerType,
                            final KeyInputState globalKeyInputState)
            throws AppiumException {

        // TODO: Use globalKeyInputState to get metaState
        int metaState = 0;

        (new MotionEventBuilder(uiController))
                .setAction(ACTION_DOWN)
                .setButtonState(extractButton(button, pointerType))
                .setDownTime(downTime)
                .setX(x)
                .setY(y)
                .setMetaState(metaState)
                .run();
    }

    public void pointerUp(long x, long y,
                          Integer button, PointerType pointerType,
                          final KeyInputState globalKeyInputState)
            throws AppiumException {

        // TODO: Use globalKeyInputState to get metaState
        int metaState = 0;

        (new MotionEventBuilder(uiController))
                .setAction(ACTION_UP)
                .setButtonState(extractButton(button, pointerType))
                .setDownTime(downTime)
                .setX(x)
                .setY(y)
                .setMetaState(metaState)
                .run();
    }

    public void pointerMove(long x, long y, final KeyInputState globalKeyInputState) throws AppiumException {
        // TODO: Use globalKeyInputState to get metaState
        int metaState = 0;

        (new MotionEventBuilder(uiController))
                .setAction(ACTION_MOVE)
                .setDownTime(downTime)
                .setX(x)
                .setY(y)
                .setMetaState(metaState)
                .run();
    }

    public static synchronized AndroidMotionEvent getMotionEvent(
            String sourceId, UiController uiController) {
        if (!motionEvents.containsKey(sourceId)) {
            motionEvents.put(sourceId, new AndroidMotionEvent(uiController));
        }
        return motionEvents.get(sourceId);
    }
}
