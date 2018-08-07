package io.appium.espressoserver.lib.handlers;

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewInteraction;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MotionEventBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.MotionEventParams;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.doubleClick;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.MOUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class PointerEventHandler implements RequestHandler<MotionEventParams, Void> {

    private final TouchType touchType;
    @Nullable
    private static MotionEvent globalTouchDownEvent;
    private static Map<Integer, MotionEvent> globalMouseButtonDownEvents; // Map mouse down events to android MotionEvent
    private static final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

    public enum TouchType {
        CLICK,
        DOUBLE_CLICK,
        LONG_CLICK,
        SCROLL,
        TOUCH_DOWN,
        TOUCH_UP,
        TOUCH_MOVE,
        TOUCH_SCROLL,
        MOUSE_UP,
        MOUSE_DOWN,
        MOUSE_MOVE;
    }

    public PointerEventHandler(TouchType touchType) {
        this.touchType = touchType;
    }

    @Override
    public Void handle(MotionEventParams params) throws AppiumException {
        switch (touchType) {
            case CLICK:
                handleClick(params);
                break;
            case DOUBLE_CLICK:
                handleDoubleClick(params);
                break;
            case LONG_CLICK:
                handleLongClick(params);
                break;
            case TOUCH_DOWN:
                handleTouchDown(params);
                break;
            case TOUCH_UP:
                handleTouchUp(params);
                break;
            case TOUCH_MOVE:
                handleTouchMove(params);
                break;
            case TOUCH_SCROLL:
                handleTouchScroll(params);
                break;
            case MOUSE_DOWN:
                handleMouseButtonDown(params);
            case MOUSE_UP:
                handleMouseButtonUp(params);
            case MOUSE_MOVE:
                handleMouseMove(params);
            default:
                break;
        }
        return null;
    }

    private static void checkBounds(Long x, Long y) throws AppiumException {
        if (x < 0 || y < 0 || x > displayMetrics.widthPixels || y > displayMetrics.heightPixels) {
            throw new AppiumException(String.format("Coordinates [%s %s] are outside of viewport [%s %s]",
                    x, y, displayMetrics.widthPixels, displayMetrics.heightPixels));
        }
    }

    private static synchronized MotionEvent handlePointerEvent(final MotionEventParams params,
                                           final int action,
                                           final PointerType pointerType,
                                           @Nullable final Long downTime,
                                           @Nullable final Long eventTime)
            throws AppiumException {
        checkBounds(params.getX(), params.getY());
        final UiControllerRunnable<MotionEvent> runnable = new UiControllerRunnable<MotionEvent>() {
            @Override
            public MotionEvent run(UiController uiController) throws AppiumException {
                return new MotionEventBuilder()
                        .withDownTime(downTime == null ? SystemClock.uptimeMillis() : downTime)
                        .withEventTime(eventTime == null ? SystemClock.uptimeMillis() : eventTime)
                        .withX(params.getX())
                        .withY(params.getY())
                        .withPointerType(pointerType)
                        .withButtonState(params.getAndroidButtonState())
                        .withAction(action)
                        .build()
                        .run(uiController);
            }
        };

        UiControllerPerformer<MotionEvent> uiControllerPerformer = new UiControllerPerformer<>(runnable);
        return uiControllerPerformer.run();
    }

    private static synchronized MotionEvent handlePointerEvent(final MotionEventParams params,
                                                               final int action,
                                                               final PointerType pointerType,
                                                               final Long downTime)
            throws AppiumException {
        return handlePointerEvent(params, action, pointerType, downTime, SystemClock.uptimeMillis());
    }

    private static synchronized MotionEvent handlePointerEvent(final MotionEventParams params,
                                           final int action,
                                           final PointerType pointerType)
            throws AppiumException {
        return handlePointerEvent(params, action, pointerType, SystemClock.uptimeMillis(), SystemClock.uptimeMillis());
    }


    private void handleTouchDown(final MotionEventParams params) throws AppiumException {
        if (globalTouchDownEvent != null) {
            throw new AppiumException("Cannot call touch down while another touch event is still down");
        }
        AndroidLogger.logger.info(String.format("Calling touch down event on (%s %s)", params.getX(), params.getY()));
        globalTouchDownEvent = handlePointerEvent(params, ACTION_DOWN, TOUCH);
    }

    private void handleTouchUp(final MotionEventParams params) throws AppiumException {
        AndroidLogger.logger.info(String.format("Calling touch up event on (%s %s)", params.getX(), params.getY()));
        if (globalTouchDownEvent == null) {
            throw new AppiumException("Touch up event must be preceded by a touch down event");
        }
        handlePointerEvent(params, ACTION_UP, TOUCH, globalTouchDownEvent.getDownTime());
        globalTouchDownEvent = null;
    }

    private void handleTouchMove(final MotionEventParams params) throws AppiumException {
        if (globalTouchDownEvent == null) {
            throw new AppiumException("Touch move event must have a touch down event");
        }
        handlePointerEvent(params, ACTION_MOVE, TOUCH, globalTouchDownEvent.getDownTime());
    }

    private void handleTouchScroll(final MotionEventParams params) throws AppiumException {
        // Fabricate a scroll event
        long startX;
        long startY;
        if (params.getElementId() != null) {
            final View view = Element.getViewById(params.getElementId());
            final ViewElement viewElement = new ViewElement(view);
            startX = viewElement.getBounds().left;
            startY = viewElement.getBounds().top;
        } else {
            // If no element provided, do halfway points
            startX = (displayMetrics.widthPixels / 2) - (params.getX() / 2);
            startY = (displayMetrics.heightPixels / 2) - (params.getY() / 2);
        }

        // Do down event
        MotionEventParams downParams = new MotionEventParams();
        downParams.setX(startX);
        downParams.setY(startY);
        MotionEvent downEvent = handlePointerEvent(downParams, ACTION_DOWN, TOUCH);

        Long downTime = downEvent.getDownTime();
        Long eventTime = downTime;

        // For it to be considered a 'scroll', must hold down for longer then tap timeout duration
        long scrollDuration = (long) (ViewConfiguration.getTapTimeout() * 1.5);

        eventTime += scrollDuration;
        MotionEventParams moveParams = new MotionEventParams();
        moveParams.setX(startX + params.getX());
        moveParams.setY(startY + params.getY());
        handlePointerEvent(moveParams, ACTION_MOVE, TOUCH, downTime, eventTime);

        // Release finger after another 'scroll' duration
        eventTime += scrollDuration;
        MotionEventParams upParams = new MotionEventParams();
        upParams.setX(startX + params.getX());
        upParams.setY(startY + params.getY());
        handlePointerEvent(upParams, ACTION_UP, TOUCH, downTime, eventTime);
    }

    private void handleMouseButtonDown(final MotionEventParams params) throws AppiumException {
        MotionEvent mouseDownEvent = handlePointerEvent(params, ACTION_DOWN, MOUSE);
        globalMouseButtonDownEvents.put(params.getButton(), mouseDownEvent);
        handlePointerEvent(params, ACTION_DOWN, MOUSE, SystemClock.uptimeMillis());
    }

    private void handleMouseButtonUp(final MotionEventParams params) throws AppiumException {
        MotionEvent mouseDownEvent = globalMouseButtonDownEvents.get(params.getButton());
        if (mouseDownEvent == null) {
            throw new AppiumException(String.format(
                    "Mouse button up event '%s' must be preceded by a mouse down event",
                    params.getButton()
            ));
        }
        handlePointerEvent(params, ACTION_UP, MOUSE, mouseDownEvent.getDownTime());
        globalMouseButtonDownEvents.remove(params.getButton());
    }

    private void handleMouseMove(final MotionEventParams params) throws AppiumException {
        params.setButton(getGlobalButtonState());
        handlePointerEvent(params, ACTION_MOVE, MOUSE);
    }

    private void handleClick(final MotionEventParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(click());
    }

    private void handleDoubleClick(final MotionEventParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for double click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(doubleClick());
    }

    private void handleLongClick(final MotionEventParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for long click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(longClick());
    }

    private int getGlobalButtonState() throws InvalidArgumentException {
        int buttonState = 0;
        for(final Map.Entry<Integer, MotionEvent> mouseDownEventEntry: globalMouseButtonDownEvents.entrySet()) {
            buttonState |= MotionEventParams.getAndroidButtonState(mouseDownEventEntry.getKey());
        }
        return buttonState;
    }
}
