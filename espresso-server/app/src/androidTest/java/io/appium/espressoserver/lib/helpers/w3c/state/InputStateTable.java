package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;

/**
 * Keep the state of all active input sources
 *
 * (defined in 17.1 of spec)
 */
public class InputStateTable {

    private final Map<String, InputState> stateTable = new HashMap<>();
    private final List<ActionObject> cancelList = new ArrayList<>();

    private static Map<String, InputStateTable> inputStateTables = new HashMap<>();

    public void addInputState(String id, InputState inputState){
        stateTable.put(id, inputState);
    }

    public InputState getInputState(String id) {
        return stateTable.get(id);
    }

    public InputState getOrCreateInputState(String sourceId, ActionObject actionObject) {
        if (!this.hasInputState(sourceId)) {
            InputState newInputState = null;
            switch(actionObject.getType()) {
                case KEY:
                    newInputState = new KeyInputState();
                    break;
                case POINTER:
                    newInputState = new PointerInputState(actionObject.getPointer());
                    break;
                // Don't need to track state of null input types
                case NONE:
                default:
                    break;
            }
            if (newInputState != null) {
                this.addInputState(sourceId, newInputState);
            }
        }
        return stateTable.get(sourceId);
    }

    public boolean hasInputState(String id) {
        return stateTable.containsKey(id);
    }

    public void addActionToCancel(ActionObject actionObject) {
        cancelList.add(actionObject);
    }

    public List<ActionObject> getCancelList() {
        return Collections.unmodifiableList(cancelList);
    }

    public KeyInputState getGlobalKeyInputState() {
        List<KeyInputState> keyInputStates = new ArrayList<>();
        for(Map.Entry<String, InputState> inputStateEntry:stateTable.entrySet()) {
            InputState inputState = inputStateEntry.getValue();
            if (inputState.getClass() == KeyInputState.class) {
                keyInputStates.add((KeyInputState) inputState);
            }
        }
        return KeyInputState.getGlobalKeyState(keyInputStates);
    }

    /**
     * Do the release actions to undo everything
     * @param adapter W3C Action adapter
     * @param timeAtBeginningOfTick When did the tick begin
     * @throws AppiumException
     */
    public void undoAll (W3CActionAdapter adapter, long timeAtBeginningOfTick)
            throws AppiumException {
        // 2-3: Dispatch tick actions with arguments undo actions and duration 0 in reverse order
        Collections.reverse(cancelList);
        for (ActionObject actionObject:cancelList) {
            actionObject.dispatch(adapter, this, 0, timeAtBeginningOfTick);
        }
        adapter.sychronousTickActionsComplete();

        // Clear the cancel list now that the Undo operations are all fulfilled
        cancelList.clear();
    }


    /**
     * Get the global input states for a given session
     * @param sessionId ID of the session
     * @return
     */
    public synchronized static InputStateTable getInputStateTableOfSession(String sessionId) {
        InputStateTable globalInputStateTable = inputStateTables.get(sessionId);
        if (globalInputStateTable == null) {
            inputStateTables.put(sessionId, new InputStateTable());
            globalInputStateTable = inputStateTables.get(sessionId);
        }
        return globalInputStateTable;
    }
}
