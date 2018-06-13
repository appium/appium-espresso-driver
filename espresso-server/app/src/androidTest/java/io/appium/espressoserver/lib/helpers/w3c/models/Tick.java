package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.state.InputState;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER;

public class Tick implements Iterator<ActionObject> {

    private final List<ActionObject> tickActions = new ArrayList<>();
    private int actionCounter = 0;

    public void addAction(ActionObject action) {
        tickActions.add(action);
    }

    @Override
    public boolean hasNext() {
        return actionCounter < tickActions.size();
    }

    @Override
    public ActionObject next() {
        return tickActions.get(actionCounter++);
    }

    /**
     * Get max tick duration for a tick
     * @return Max tick duration
     */
    public long calculateTickDuration(){
        long maxDuration = 0;
        for(ActionObject actionObject: tickActions) {
            long currDuration = 0;

            InputSourceType type = actionObject.getType();
            Long duration = actionObject.getDuration();
            ActionType subType = actionObject.getSubType();

            if (duration != null) {
                if (type == POINTER && subType == POINTER_MOVE) {
                    currDuration = duration;
                } else if (subType == PAUSE) {
                    currDuration = duration;
                }
            }

            if (currDuration > maxDuration) {
                maxDuration = currDuration;
            }
        }
        return maxDuration;
    }

    public List<Callable<Void>> dispatch(W3CActionAdapter adapter, InputStateTable inputStateTable, long tickDuration)
            throws AppiumException {
        long timeAtBeginningOfTick = System.currentTimeMillis();
        KeyInputState globalKeyInputState = inputStateTable.getGlobalKeyInputState();

        List<Callable<Void>> asyncOperations = new ArrayList<>();
        for(ActionObject actionObject: tickActions) {
            String sourceId = actionObject.getId();

            // 1.3 If the current session's input state table doesn't have a property corresponding to
            // source id, then let the property corresponding to source id be a new object of the
            // corresponding input source state type for source type.
            if (!inputStateTable.hasInputState(sourceId)) {
                InputState newInputState = null;
                switch(actionObject.getType()) {
                    case KEY:
                        newInputState = new KeyInputState();
                        break;
                    case POINTER:
                        newInputState = new PointerInputState();
                        break;
                    case NONE:
                        // Don't need to track state of null input types
                        break;
                    default:
                        break;
                }
                if (newInputState != null) {
                    inputStateTable.addInputState(sourceId, newInputState);
                }
            }

            // 1.4 Let device state be the input source state corresponding to source id in the current session’s input state table
            InputState deviceState = inputStateTable.getInputState(sourceId);

            // 2. Run algorithm with arguments source id, action object, device state and tick duration
            Callable<Void> dispatchResult = actionObject.dispatch(adapter, deviceState, inputStateTable, tickDuration, timeAtBeginningOfTick);
            if (dispatchResult != null) {
                asyncOperations.add(dispatchResult);
            }
        }

        return asyncOperations;
    }

}
