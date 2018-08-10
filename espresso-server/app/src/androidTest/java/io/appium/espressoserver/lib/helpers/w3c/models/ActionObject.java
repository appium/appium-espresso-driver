package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.InputState;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyUp;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerCancel;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerUp;
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
     * @param inputStateTable State of all inputs
     * @param tickDuration How long the tick is
     * @param timeAtBeginningOfTick When the tick began
     * @return
     * @throws AppiumException
     */
    @Nullable
    public Callable<BaseDispatchResult> dispatch(W3CActionAdapter adapter,
                                                 InputStateTable inputStateTable,
                                                 long tickDuration, long timeAtBeginningOfTick) throws AppiumException {
        InputSourceType inputSourceType = this.getType();
        ActionType actionType = this.getSubType();
        adapter.getLogger().info(String.format(
                "Dispatching action #%s of input source %s",
                index, getId()
        ));
        // 1.3 If the current session's input state table doesn't have a property corresponding to
        //      source id, then let the property corresponding to source id be a new object of the
        //      corresponding input source state type for source type.
        // 1.4 Let device state be the input source state corresponding to source id in the current session’s input state table
        InputState deviceState = inputStateTable.getOrCreateInputState(this.getId(), this);
        try {

            if (inputSourceType == KEY) {
                switch (actionType) {
                    case KEY_DOWN:
                        dispatchKeyDown(adapter, this, (KeyInputState) deviceState, inputStateTable, tickDuration);
                        break;
                    case KEY_UP:
                        dispatchKeyUp(adapter, this, (KeyInputState) deviceState, inputStateTable, tickDuration);
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
                                (PointerInputState) deviceState,
                                tickDuration,
                                timeAtBeginningOfTick,
                                inputStateTable.getGlobalKeyInputState()
                        );
                    case POINTER_DOWN:
                        dispatchPointerDown(
                                adapter,
                                this,
                                (PointerInputState) deviceState,
                                inputStateTable,
                                inputStateTable.getGlobalKeyInputState()
                        );
                        return null;
                    case POINTER_UP:
                        dispatchPointerUp(
                                adapter,
                                this,
                                (PointerInputState) deviceState,
                                inputStateTable,
                                inputStateTable.getGlobalKeyInputState()
                        );
                        return null;
                    case POINTER_CANCEL:
                        dispatchPointerCancel(
                                adapter,
                                this
                        );
                        return null;
                    default:
                        adapter.getLogger().info(String.format(
                                "Dispatching pause event for %s milliseconds",
                                getDuration()
                        ));
                        break;
                }
            }
        } catch (ClassCastException cce) {
            throw new InvalidArgumentException(String.format(
                    "Attempted to apply action of type '%s' to a source with type '%s': %s",
                    inputSourceType, deviceState.getClass().getSimpleName(), cce.getMessage()
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

    public long getX() {
        return x == null ? 0 : x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public long getY() {
        return y == null ? 0 : y;
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

    public void setId(String id) {
        this.id = id;
    }
}
