package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import androidx.test.espresso.UiController;
import androidx.test.espresso.action.GeneralLocation;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.BaseW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.model.Element;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.isTouch;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.toCoordinates;

public class EspressoW3CActionAdapter extends BaseW3CActionAdapter {

    private final AndroidKeyEvent androidKeyEvent;
    private UiController uiController;
    private final MultiTouchState multiTouchState = new MultiTouchState();
    private final DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

    public EspressoW3CActionAdapter(UiController uiController) {
        this.uiController = uiController;
        this.androidKeyEvent = new AndroidKeyEvent(uiController);
    }

    public void keyDown(final W3CKeyEvent keyEvent) throws AppiumException {
        androidKeyEvent.keyDown(keyEvent);
    }

    public void keyUp(final W3CKeyEvent keyEvent) throws AppiumException {
        androidKeyEvent.keyUp(keyEvent);
    }

    public void pointerDown(int button, String sourceId, PointerType pointerType,
                            Float x, Float y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer down at coordinates: %s %s %s", x, y, pointerType));
        final Point roundedCoords = toCoordinates(x, y);

        if (isTouch(pointerType)) {
            // touch down actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_DOWN, sourceId,  (long) roundedCoords.x, (long) roundedCoords.y, globalKeyInputState, button);
        } else {
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            List<Long> xList = Collections.singletonList((long) roundedCoords.x);
            List<Long> yList = Collections.singletonList((long) roundedCoords.y);
            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_DOWN, button, pointerType, globalKeyInputState, null, 0);

            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_POINTER_DOWN, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerUp(int button, String sourceId, PointerType pointerType,
                          Float x, Float y, Set<Integer> depressedButtons,
                   KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer up at coordinates: %s %s %s", x, y, pointerType));
        final Point roundedCoords = toCoordinates(x, y);
        if (isTouch(pointerType)) {
            // touch up actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_UP, sourceId, (long) roundedCoords.x, (long) roundedCoords.y, globalKeyInputState, button);
        } else {
            List<Long> xList = Collections.singletonList((long) roundedCoords.x);
            List<Long> yList = Collections.singletonList((long) roundedCoords.y);
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_POINTER_UP, button, pointerType, globalKeyInputState, null, 0);
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_UP, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerMove(String sourceId, PointerType pointerType,
                            Float currentX, Float currentY, Float x, Float y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer move at coordinates: %s %s %s", x, y, pointerType));
        final Point roundedCoords = toCoordinates(x, y);
        if (isTouch(pointerType)) {
            multiTouchState.updateTouchState(ACTION_MOVE, sourceId, (long) roundedCoords.x, (long) roundedCoords.y,  globalKeyInputState, null);
            multiTouchState.pointerMove(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                    .pointerMove(Collections.singletonList((long) roundedCoords.x), Collections.singletonList((long) roundedCoords.y), pointerType, globalKeyInputState, null);
        }
    }

    public void pointerCancel(String sourceId, PointerType pointerType) throws AppiumException {
        if (isTouch(pointerType)) {
            multiTouchState.pointerCancel(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController).pointerCancel();
        }
    }

    public void sychronousTickActionsComplete() throws AppiumException {
        multiTouchState.perform(uiController);
        AndroidLogger.logger.info("Pointer event: Tick complete");
    }

    public int getKeyCode(String keyValue, int location) {
        return AndroidKeyEvent.getKeyCode(keyValue, location);
    }

    public long getViewportHeight() {
        return displayMetrics.heightPixels;
    }

    public long getViewportWidth() {
        return displayMetrics.widthPixels;
    }

    public Point getElementCenterPoint(String elementId)
            throws AppiumException {
        View view = Element.Companion.getViewById(elementId);
        float[] coords = GeneralLocation.CENTER.calculateCoordinates(view);
        Point point = new Point();
        point.x = Math.round(coords[0]);
        point.y = Math.round(coords[1]);
        return point;
    }

    public void waitForUiThread() {
        uiController.loopMainThreadUntilIdle();
    }

    public void sleep(Float duration) {
        long roundedDuration = Math.round(duration);
        if (duration != roundedDuration) {
            this.getLogger().warn(String.format("Rounding provided duration %sms to %sms", duration, roundedDuration));
        }
        uiController.loopMainThreadForAtLeast(roundedDuration);
    }
    
    public Logger getLogger() {
        return AndroidLogger.logger;
    }
}
