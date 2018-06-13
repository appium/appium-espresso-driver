package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.InputState;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyUp;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER;

public class ActionObject {
    private int index;
    private InputSourceType type;
    private ActionType subType;
    private String id;
    private Long duration;
    private Long x;
    private Long y;
    private int button;
    private String value;
    private PointerType pointer;
    private Origin origin = new Origin();

    public ActionObject() {

    }

    // Copy constructor
    public ActionObject(ActionObject actionObject) {
        this.index = actionObject.index;
        this.type = actionObject.type;
        this.subType = actionObject.subType;
        this.id = actionObject.id;
        this.duration = actionObject.duration;
        this.origin = actionObject.origin;
        this.x = actionObject.x;
        this.y = actionObject.y;
        this.button = actionObject.button;
        this.value = actionObject.value;
        this.pointer = actionObject.pointer;
    }

    public ActionObject(String id, InputSourceType type, ActionType subType, int index){
        this.type = type;
        this.subType = subType;
        this.id = id;
        this.index = index; // Store the index of the action for possible future logging issues
    }

    /**
     * Call `dispatch tick actions` algorithm in section 17.4
     * @param adapter Adapter for actions
     * @param inputState State of the corresponding input
     * @param inputStateTable State of all inputs
     * @param tickDuration How long the tick is
     * @param timeAtBeginningOfTick When the tick began
     * @return
     * @throws AppiumException
     */
    @Nullable
    public Callable<Void> dispatch(W3CActionAdapter adapter,
                             InputState inputState,
                             InputStateTable inputStateTable,
                             long tickDuration, long timeAtBeginningOfTick) throws AppiumException {
        InputSourceType inputSourceType = this.getType();
        ActionType actionType = this.getSubType();
        try {
            if (inputSourceType == KEY) {
                switch (actionType) {
                    case KEY_DOWN:
                        dispatchKeyDown(adapter, this, (KeyInputState) inputState, inputStateTable, tickDuration);
                        break;
                    case KEY_UP:
                        dispatchKeyUp(adapter, this, (KeyInputState) inputState, inputStateTable, tickDuration);
                        break;
                    case PAUSE:
                    default:
                        break;
                }
            } else if (inputSourceType == POINTER) {
                switch (actionType) {
                    case POINTER_MOVE:
                        return dispatchPointerMove(
                                adapter,
                                this.getId(),
                                this,
                                (PointerInputState) inputState,
                                tickDuration,
                                System.currentTimeMillis() - timeAtBeginningOfTick,
                                inputStateTable.getGlobalKeyInputState()
                        );
                    default:
                        break;
                }
            }
        } catch (ClassCastException cce) {
            throw new InvalidArgumentException(String.format(
                    "Attempted to apply action of type '%s' to a source with type '%s'",
                    inputSourceType, inputStateTable.getClass().getSimpleName()
            ));
        }
        return null;
    }

    public InputSourceType getType() {
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }

    public ActionType getSubType() {
        return subType;
    }

    public void setSubType(ActionType subType) {
        this.subType = subType;
    }

    @Nullable
    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Nullable
    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @Nullable
    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    @Nullable
    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setPointer(PointerType pointer) {
        this.pointer = pointer;
    }

    public PointerType getPointer() {
        return pointer;
    }

    public String getId() {
        return id;
    }
}
