package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Keep the state of all active input sources
 *
 * (defined in 17.1 of spec)
 */
public class InputStateTable {

    private final Map<String, InputState> stateTable = new HashMap<>();
    private static InputStateTable globalInputStateTable;

    public void addInputState(String id, InputState inputState){
        stateTable.put(id, inputState);
    }

    public InputState getInputState(String id) {
        return stateTable.get(id);
    }

    public boolean hasInputState(String id) {
        return stateTable.containsKey(id);
    }

    public synchronized static InputStateTable getInstance() {
        if (globalInputStateTable == null) {
            globalInputStateTable = new InputStateTable();
        }
        return globalInputStateTable;
    }
}
