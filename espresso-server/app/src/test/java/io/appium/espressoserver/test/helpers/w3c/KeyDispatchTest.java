package io.appium.espressoserver.test.helpers.w3c;

import org.junit.Test;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyUp;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class KeyDispatchTest {

    @Test
    public void shouldToggleAltIfAltPassedToBlankKeyState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        String alt = "\uE00A";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(alt);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isAlt());
        assertFalse(keyInputState.isCtrl());
        List<ActionObject> cancelList = inputStateTable.getCancelList();
        assertTrue(cancelList.isEmpty());
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable);
        assertEquals(cancelList.size(), 1);
        ActionObject cancelObject = cancelList.get(0);
        assertEquals(cancelObject.getSubType(), KEY_UP);
        assertEquals(cancelObject.getValue(), alt);
        assertTrue(keyInputState.isAlt());
        assertFalse(keyInputState.isCtrl());
    }

    @Test
    public void shouldAddKeysToKeyPressedState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        String value = "F";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(value);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isPressed(value));
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable);
        assertTrue(keyInputState.isPressed(value));
    }

    @Test
    public void shouldPassKeyCodeToEvent() throws AppiumException {

        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue("F");
        KeyInputState keyInputState = new KeyInputState();
        W3CKeyEvent keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable);
        assertEquals(keyEvent.getCode(), "KeyF");
    }

    @Test
    public void shouldPassNullKeyCodeAndLocationZeroIfNotDefined() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue("\uFFFF");
        KeyInputState keyInputState = new KeyInputState();
        W3CKeyEvent keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable);
        assertNull(keyEvent.getCode());
        assertEquals(keyEvent.getLocation(), 0);
    }

    @Test
    public void shouldReleaseAltOnAltKeyUp() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        String alt = "\uE00A";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(alt);
        KeyInputState keyInputState = new KeyInputState();
        keyInputState.addPressed("ALT");
        keyInputState.setAlt(true);
        keyInputState.setCtrl(true);
        assertTrue(keyInputState.isAlt());
        assertTrue(keyInputState.isCtrl());
        dispatchKeyUp(adapter, actionObject, keyInputState, inputStateTable);
        assertFalse(keyInputState.isAlt());
        assertTrue(keyInputState.isCtrl());
    }

    @Test
    public void shouldAddAndRemoveKeysFromKeyPressedState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        String id = "keyboard";
        String value = "~";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(value);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isPressed(value));

        // Press down a key and check that it's pressed
        dispatchKeyDown(adapter, actionObject, keyInputState, inputStateTable);
        assertTrue(keyInputState.isPressed(value));

        // Release a key and check that it's not pressed
        dispatchKeyUp(adapter, actionObject, keyInputState, inputStateTable);
        assertFalse(keyInputState.isPressed(value));
    }

    @Test
    public void shouldNormalizeKeys() throws AppiumException {
        String normalizedKey = KeyNormalizer.getInstance().getNormalizedKey("\uE008");
        assertEquals(normalizedKey, NormalizedKeys.SHIFT);
    }
}
