package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.support.test.espresso.UiController;
import android.view.MotionEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static android.view.MotionEvent.ACTION_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.extractButton;

public class AndroidMotionEvent {

    private static AndroidMotionEvent touchMotionEvent;
    private long downTime;
    private final UiController uiController;
    private static final Map<String, AndroidMotionEvent> motionEvents = new ConcurrentHashMap<>();

    private AndroidMotionEvent(UiController uiController) {
        this.uiController = uiController;
    }

    public MotionEvent pointerUpOrDown(List<Long> x, List<Long> y,
                                       int action,
                                       Integer button, PointerType pointerType,
                                       final KeyInputState globalKeyInputState,
                                       final MotionEvent downEvent,
                                       final long eventTime)
            throws AppiumException {

        // TODO: Use globalKeyInputState to get metaState
        int metaState = 0;

        this.downTime = eventTime;

        return (new MotionEventBuilder(uiController))
                .setAction(action)
                .setButtonState(extractButton(button, pointerType))
                .setPointerType(pointerType)
                .setDownTime(downTime)
                .setEventTime(eventTime)
                .setX(x)
                .setY(y)
                .setMetaState(metaState)
                .setSource(downEvent != null ? downEvent.getSource() : 0)
                .run();
    }

    public void pointerMove(List<Long> x, List<Long> y,
                            final PointerType pointerType,
                            final KeyInputState globalKeyInputState,
                            final MotionEvent downEvent) throws AppiumException {
        // TODO: Use globalKeyInputState to get metaState
        int metaState = 0;

        (new MotionEventBuilder(uiController))
                .setAction(ACTION_MOVE)
                .setDownTime(downTime)
                .setPointerType(pointerType)
                .setX(x)
                .setY(y)
                .setMetaState(metaState)
                .setSource(downEvent.getSource())
                .run();
    }

    public static synchronized AndroidMotionEvent getMotionEvent(
            String sourceId, UiController uiController) {
        if (!motionEvents.containsKey(sourceId)) {
            motionEvents.put(sourceId, new AndroidMotionEvent(uiController));
        }
        return motionEvents.get(sourceId);
    }

    public static synchronized AndroidMotionEvent getTouchMotionEvent(UiController uiController) {
        if (touchMotionEvent == null) {
            touchMotionEvent = new AndroidMotionEvent(uiController);
        }
        return touchMotionEvent;
    }
}
