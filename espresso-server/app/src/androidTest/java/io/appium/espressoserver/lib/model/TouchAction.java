package io.appium.espressoserver.lib.model;

import android.view.ViewConfiguration;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceBuilder;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Parameters;
import io.appium.espressoserver.lib.helpers.w3c.models.Origin;

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

    public List<Action> toW3CAction() throws InvalidArgumentException {
        List<Action> w3cActions;
        switch (action) {
            case MOVE_TO:
                w3cActions = Collections.singletonList(convertMoveTo());
                break;
            case PRESS:
                w3cActions = convertPress(PRESS_DURATION);
                break;
            case LONG_PRESS:
                w3cActions = convertPress(LONG_PRESS_TIMEOUT + TIMEOUT_BUFFER);
                break;
            case TAP:
                w3cActions = convertPress(TAP_TIMEOUT - TIMEOUT_BUFFER);
                break;
            case RELEASE:
                w3cActions = Collections.singletonList(convertRelease());
                break;
            case WAIT:
                w3cActions = Collections.singletonList(convertWait());
                break;
            case CANCEL:
                w3cActions = Collections.singletonList(convertCancel());
                break;
            default:
                throw new InvalidArgumentException(String.format("Unsupported action type %s", action));
        }

        // All touch actions map to 3 actions
        // For multi-touch actions we need each event to happen synchronously with eachother

        // e.g.) if one input calls press (which maps to move + down + wait) and another input is
        // calling pause (which maps to wait) we need to add two no-ops to the wait event so that it
        // doesn't prematurely advance to the next action before the 'press' event finishes
        return padActionsList(w3cActions);
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

    private Origin getOrigin() {
        Origin origin = new Origin();
        if (options.getElementId() != null) {
            origin.setType(InputSource.ELEMENT);
            origin.setElementId(options.getElementId());
        } else {
            origin.setType(InputSource.VIEWPORT);
        }
        return origin;
    }

    private Action getMoveTo() {
        return new ActionBuilder()
                .withType(POINTER_MOVE)
                .withX(options.getX())
                .withY(options.getY())
                .withOrigin(getOrigin())
                .build();
    }

    private Action convertMoveTo() {
        return getMoveTo();
    }

    private List<Action> convertPress(Long pressDuration) {
        // Move to spot
        Action moveAction = getMoveTo();

        // Press down
        Action downAction = new ActionBuilder()
                .withType(POINTER_DOWN)
                .build();

        // Wait for the press duration
        Action waitAction = new ActionBuilder()
                .withType(PAUSE)
                .withDuration(pressDuration)
                .build();

        return Arrays.asList(moveAction, downAction, waitAction);
    }

    private Action getPause() {
        return new ActionBuilder()
                .withType(PAUSE)
                .withDuration(0L)
                .build();
    }

    // If an action list has fewer than three actions, pad them with 'pauses' of 0 duration
    private List<Action> padActionsList(List<Action> actions) {
        // One Jsonwp Touch Action maps to three W3C Actions
        final int W3C_ACTIONS_PER_TOUCH_ACTION = 3;

        List<Action> paddedActions = new ArrayList<>(3);
        for (int padIndex = 0; padIndex < W3C_ACTIONS_PER_TOUCH_ACTION - actions.size(); padIndex++) {
            paddedActions.add(getPause());
        }
        for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
            paddedActions.add(actions.get(actionIndex));
        }

        return paddedActions;
    }

    public static List<InputSource> toW3CInputSources(List<List<TouchAction>> touchActionsLists) throws AppiumException {
        int touchInputIndex = 0;

        List<InputSource> inputSources = new ArrayList<>();
        for (List<TouchAction> touchActions: touchActionsLists) {
            List<Action> w3cActions = new ArrayList<>();
            for (TouchAction touchAction: touchActions) {
                w3cActions.addAll(touchAction.toW3CAction());
            }

            Parameters parameters = new Parameters();
            parameters.setPointerType(TOUCH);

            // Add a finger pointer
            inputSources.add(new InputSourceBuilder()
                    .withType(POINTER)
                    .withParameters(parameters)
                    .withId(String.format("finger%s", touchInputIndex++))
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

        @Nullable
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
