package io.appium.espressoserver.lib.model;

import android.view.ViewConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Parameters;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_CANCEL;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class TouchAction {

    private final long TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private final long LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    // Make the standard press duration be between TAP and LONG_PRESS timeouts
    private final long PRESS_DURATION = (TAP_TIMEOUT + LONG_PRESS_TIMEOUT) / 2;

    // Number of ms to add or subtract to a timeout so that it isn't the exact number
    private final long TIMEOUT_BUFFER = 10;

    private ActionType action;
    private TouchActionOptions options;

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public TouchActionOptions getOptions() {
        return options;
    }

    public void setOptions(TouchActionOptions options) {
        this.options = options;
    }

    public List<Action> toW3CAction() {
        switch (action) {
            case MOVE_TO:
                return Collections.singletonList(convertMoveTo());
            case PRESS:
                return convertPress(PRESS_DURATION);
            case LONG_PRESS:
                return convertPress(LONG_PRESS_TIMEOUT + TIMEOUT_BUFFER);
            case TAP:
                return convertPress(TAP_TIMEOUT - TIMEOUT_BUFFER);
            case RELEASE:
                return Collections.singletonList(convertRelease());
            case WAIT:
                return Collections.singletonList(convertWait());
            case CANCEL:
                return Collections.singletonList(convertCancel());
            default:
                break;
        }

        return null;
    }

    private Action convertCancel() {
        return new ActionBuilder()
                .withType(POINTER_CANCEL)
                .build();
    }

    private Action convertWait() {
        return new ActionBuilder()
                .withType(PAUSE)
                .build();
    }

    private Action convertRelease() {
        return new ActionBuilder()
                .withType(POINTER_UP)
                .build();
    }

    private Action getMoveTo(Long duration) {
        ActionBuilder actionBuilder = new ActionBuilder()
                .withType(POINTER_MOVE)
                .withDuration(duration)
                .withX(options.getX())
                .withY(options.getY());

        if (options.getElementId() != null) {
            actionBuilder.withElementId(options.getElementId());
        } else {
            actionBuilder.withOrigin(InputSource.VIEWPORT);
        }

        return actionBuilder.build();

    }

    private Action convertMoveTo() {
        return getMoveTo(0L);
    }

    private List<Action> convertPress(Long pressDuration) {
        Action moveAction = getMoveTo(pressDuration);

        Action downAction = new ActionBuilder()
                .withType(POINTER_DOWN)
                .build();

        Action upAction = new ActionBuilder()
                .withType(POINTER_DOWN)
                .build();

        List<Action> ret = new ArrayList<>();
        ret.add(moveAction);
        ret.add(downAction);
        ret.add(upAction);
        return ret;
    }

    public static List<InputSource> toW3CInputSources(List<List<TouchAction>> touchActionsLists) throws AppiumException {
        int touchInputIndex = 0;

        // Not all actions lists are the same size so we need to know the max size at each step
        boolean isMultiTouch = touchActionsLists.size() > 1;

        List<InputSource> inputSources = new ArrayList<>();
        for (List<TouchAction> touchActions: touchActionsLists) {
            List<Action> w3cActions = new ArrayList<>();
            for (TouchAction touchAction: touchActions) {
                if (isMultiTouch) {
                    // Don't accept TAP, PRESS or LONG_PRESS
                    switch (touchAction.getAction()) {
                        case TAP:
                        case PRESS:
                        case LONG_PRESS:
                            throw new InvalidArgumentException("'tap', 'press', and 'long press' are not " +
                                    "supported in multi touch events because they do not follow Android " +
                                    "consistency guarantees " +
                                    "(https://developer.android.com/reference/android/view/MotionEvent#consistency-guarantees)");
                    }
                }

                w3cActions.addAll(touchAction.toW3CAction());
            }

            Parameters parameters = new Parameters();
            parameters.setPointerType(TOUCH);

            // Add a finger pointer
            inputSources.add(new InputSourceBuilder()
                    .withType(POINTER)
                    .withParameters(parameters)
                    .withId(String.format("finger%s", touchInputIndex))
                    .withActions(w3cActions)
                    .build());
        }

        return inputSources;
    }

    public enum ActionType {
        @SerializedName("moveTo")
        MOVE_TO,
        @SerializedName("tap")
        TAP,
        @SerializedName("press")
        PRESS,
        @SerializedName("longPress")
        LONG_PRESS,
        @SerializedName("release")
        RELEASE,
        @SerializedName("wait")
        WAIT,
        @SerializedName("cancel")
        CANCEL
    }

    public static class TouchActionOptions {
        @SerializedName("element")
        private String elementId;
        private Long x;
        private Long y;

        public String getElementId() {
            return elementId;
        }

        public void setElementId(String elementId) {
            this.elementId = elementId;
        }

        public Long getX() {
            return x;
        }

        public void setX(Long x) {
            this.x = x;
        }

        public Long getY() {
            return y;
        }

        public void setY(Long y) {
            this.y = y;
        }
    }
}
