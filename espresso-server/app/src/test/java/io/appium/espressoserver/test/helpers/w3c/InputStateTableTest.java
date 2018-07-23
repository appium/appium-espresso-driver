package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Test;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class InputStateTableTest {

    @Test
    public void shouldUndoPointerDownEvents() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        W3CActionAdapter adapter = new DummyW3CActionAdapter();

        // Create a pointer down action
        ActionObject actionObject = new ActionObject();
        actionObject.setType(POINTER);
        actionObject.setSubType(POINTER_DOWN);
        actionObject.setButton(5);
        actionObject.setId("123");

        // Call the pointer down action
        actionObject.dispatch(adapter, inputStateTable, 0, 0);

        // Check that the button is depressed
        PointerInputState inputState = (PointerInputState) inputStateTable.getInputState("123");
        assertTrue(inputState.getButtons().contains(5));

        // Undo the action and check that it is not depressed anymore
        inputStateTable.undoAll(adapter, 0);
        assertFalse(inputState.getButtons().contains(5));
        assertTrue(inputState.getButtons().isEmpty());
    }

    @Test
    public void shouldUndoKeyDownEvents() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        W3CActionAdapter adapter = new DummyW3CActionAdapter();
        final String SHIFT = "\uE008";

        // Create a pointer down action
        ActionObject actionObject = new ActionObject();
        actionObject.setType(KEY);
        actionObject.setSubType(KEY_DOWN);
        actionObject.setValue(SHIFT);
        actionObject.setId("456");

        // Call the pointer down action
        actionObject.dispatch(adapter, inputStateTable, 0, 0);

        // Check that the button is depressed
        String normalizedKey = KeyNormalizer.getInstance().getNormalizedKey(SHIFT);
        KeyInputState inputState = (KeyInputState) inputStateTable.getInputState("456");
        assertTrue(inputState.isPressed(normalizedKey));
        assertTrue(inputState.isShift());

        // Undo the action and check that it is not depressed anymore
        inputStateTable.undoAll(adapter, 0);
        assertFalse(inputState.isPressed(normalizedKey));
        assertFalse(inputState.isShift());
    }
}
