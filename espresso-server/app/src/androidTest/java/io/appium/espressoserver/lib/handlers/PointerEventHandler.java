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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.MotionEventBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.model.Element;
import io.appium.espressoserver.lib.model.TouchParams;
import io.appium.espressoserver.lib.model.ViewElement;
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer;
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.doubleClick;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class PointerEventHandler implements RequestHandler<TouchParams, Void> {

    private final TouchType touchType;
    @Nullable
    private static MotionEvent globalTouchDownEvent;
    private static final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

    public enum TouchType {
        CLICK,
        DOUBLE_CLICK,
        LONG_CLICK,
        SCROLL,
        TOUCH_DOWN,
        TOUCH_UP,
        TOUCH_MOVE, TOUCH_SCROLL;

        // TODO: Add mouse events here
    }

    public PointerEventHandler(TouchType touchType) {
        this.touchType = touchType;
    }

    @Override
    public Void handle(TouchParams params) throws AppiumException {
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
            case SCROLL:
                handleScroll(params);
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

    private static synchronized MotionEvent handlePointerEvent(final TouchParams params,
                                           final int action,
                                           final PointerType pointerType,
                                           @Nullable final Long downTime,
                                           @Nullable final Long eventTime)
            throws AppiumException {
        checkBounds(params.getX(), params.getY());
        UiControllerRunnable<MotionEvent> runnable = new UiControllerRunnable<MotionEvent>() {
            @Override
            public MotionEvent run(UiController uiController) throws AppiumException {
                return new MotionEventBuilder()
                        .withDownTime(downTime == null ? SystemClock.uptimeMillis() : downTime)
                        .withEventTime(eventTime == null ? SystemClock.uptimeMillis() : eventTime)
                        .withX(params.getX())
                        .withY(params.getY())
                        .withPointerType(pointerType)
                        .withAction(action)
                        .build()
                        .run(uiController);
            }
        };

        UiControllerPerformer<MotionEvent> uiControllerPerformer = new UiControllerPerformer<>(runnable);
        return uiControllerPerformer.run();
    }

    private static synchronized MotionEvent handlePointerEvent(final TouchParams params,
                                                               final int action,
                                                               final PointerType pointerType,
                                                               final Long downTime)
            throws AppiumException {
        return handlePointerEvent(params, action, pointerType, downTime, SystemClock.uptimeMillis());
    }

    private static synchronized MotionEvent handlePointerEvent(final TouchParams params,
                                           final int action,
                                           final PointerType pointerType)
            throws AppiumException {
        return handlePointerEvent(params, action, pointerType, SystemClock.uptimeMillis(), SystemClock.uptimeMillis());
    }


    private void handleTouchDown(final TouchParams params) throws AppiumException {
        if (globalTouchDownEvent != null) {
            throw new AppiumException("Cannot call touch down while another touch event is still down");
        }
        AndroidLogger.logger.info(String.format("Calling touch down event on (%s %s)", params.getX(), params.getY()));
        globalTouchDownEvent = handlePointerEvent(params, ACTION_DOWN, TOUCH);
    }

    private void handleTouchUp(final TouchParams params) throws AppiumException {
        AndroidLogger.logger.info(String.format("Calling touch up event on (%s %s)", params.getX(), params.getY()));
        if (globalTouchDownEvent == null) {
            throw new AppiumException("Touch up event must be preceded by a touch down event");
        }
        handlePointerEvent(params, ACTION_UP, TOUCH, globalTouchDownEvent.getDownTime());
        globalTouchDownEvent = null;
    }

    private void handleTouchMove(final TouchParams params) throws AppiumException {
        if (globalTouchDownEvent == null) {
            throw new AppiumException("Touch move event must have a touch down event");
        }
        handlePointerEvent(params, ACTION_MOVE, TOUCH, globalTouchDownEvent.getDownTime());
    }

    private void handleTouchScroll(final TouchParams params) throws AppiumException {
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
        TouchParams downParams = new TouchParams();
        downParams.setX(startX);
        downParams.setY(startY);
        MotionEvent downEvent = handlePointerEvent(downParams, ACTION_DOWN, TOUCH);

        Long downTime = downEvent.getDownTime();
        Long eventTime = downTime;

        // For it to be considered a 'scroll', must hold down for longer then tap timeout duration
        long scrollDuration = (long) (ViewConfiguration.getTapTimeout() * 1.5);

        eventTime += scrollDuration;
        TouchParams moveParams = new TouchParams();
        moveParams.setX(startX + params.getX());
        moveParams.setY(startY + params.getY());
        handlePointerEvent(moveParams, ACTION_MOVE, TOUCH, downTime, eventTime);

        // Release finger after another 'scroll' duration
        eventTime += scrollDuration;
        TouchParams upParams = new TouchParams();
        upParams.setX(startX + params.getX());
        upParams.setY(startY + params.getY());
        handlePointerEvent(upParams, ACTION_UP, TOUCH, downTime, eventTime);
    }

    private void handleMouseDown(final TouchParams params) throws AppiumException {
        handlePointerEvent(params, ACTION_DOWN, PointerType.MOUSE);
    }

    private void handleMouseUp(final TouchParams params) throws AppiumException {
        handlePointerEvent(params, ACTION_UP, PointerType.MOUSE);
    }

    private void handleMouseMove(final TouchParams params) throws AppiumException {
        handlePointerEvent(params, ACTION_UP, PointerType.MOUSE);
    }

    private void handleScroll(final TouchParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for scroll event");
        }
        // Stub.
    }

    private void handleClick(final TouchParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(click());
    }

    private void handleDoubleClick(final TouchParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for double click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(doubleClick());
    }

    private void handleLongClick(final TouchParams params) throws AppiumException {
        if (params.getElementId() == null) {
            throw new InvalidArgumentException("Element ID must not be blank for long click event");
        }
        ViewInteraction viewInteraction = Element.getViewInteractionById(params.getElementId());
        viewInteraction.perform(longClick());
    }
}
