package io.appium.espressoserver.lib.helpers.w3c.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;

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

    public List<Callable<BaseDispatchResult>> dispatchAll(W3CActionAdapter adapter, InputStateTable inputStateTable, long tickDuration)
            throws AppiumException {
        long timeAtBeginningOfTick = System.currentTimeMillis();
        List<Callable<BaseDispatchResult>> asyncOperations = new ArrayList<>();
        for(ActionObject actionObject: tickActions) {
            // 2. Run algorithm with arguments source id, action object, device state and tick duration
            Callable<BaseDispatchResult> dispatchResult = actionObject.dispatch(adapter,
                    inputStateTable, tickDuration, timeAtBeginningOfTick);

            // If it's an async operation, add it to the list
            if (dispatchResult != null) {
                asyncOperations.add(dispatchResult);
            }
        }

        return asyncOperations;
    }

}
