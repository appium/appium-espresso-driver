package io.appium.espressoserver.test.helpers.w3c;

import org.junit.Test;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.KeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch.dispatchKeyUp;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class KeyDispatchTest {

    @Test
    public void shouldToggleAltIfAltPassedToBlankKeyState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        String alt = "\uE00A";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(alt);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isAlt());
        assertFalse(keyInputState.isCtrl());
        dispatchKeyDown(adapter, actionObject, keyInputState);
        assertTrue(keyInputState.isAlt());
        assertFalse(keyInputState.isCtrl());
    }

    @Test
    public void shouldAddKeysToKeyPressedState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        String value = "F";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(value);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isPressed(value));
        dispatchKeyDown(adapter, actionObject, keyInputState);
        assertTrue(keyInputState.isPressed(value));
    }

    @Test
    public void shouldReturnNullIfImplementationReturnsFalse() throws AppiumException {
        class TestAdapter extends DummyW3CActionAdapter {
            public boolean keyDown(KeyEvent keyEvent) {
                return false;
            }
        }
        TestAdapter testAdapter = new TestAdapter();
        String id = "keyboard";
        String alt = "F";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(alt);
        KeyInputState keyInputState = new KeyInputState();
        assertNull(dispatchKeyDown(testAdapter, actionObject, keyInputState));
    }

    @Test
    public void shouldPassKeyCodeToEvent() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue("F");
        KeyInputState keyInputState = new KeyInputState();
        KeyEvent keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState);
        assertEquals(keyEvent.getCode(), "KeyF");
    }

    @Test
    public void shouldPassNullKeyCodeAndLocationZeroIfNotDefined() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue("\uFFFF");
        KeyInputState keyInputState = new KeyInputState();
        KeyEvent keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState);
        assertNull(keyEvent.getCode());
        assertEquals(keyEvent.getLocation(), 0);
    }

    @Test
    public void shouldMapRawKeyToLocation() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue("\uE007");
        KeyInputState keyInputState = new KeyInputState();
        KeyEvent keyEvent = dispatchKeyDown(adapter, actionObject, keyInputState);
        assertEquals(keyEvent.getLocation(), 1);
    }

    @Test
    public void shouldReleaseAltOnAltKeyUp() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
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
        dispatchKeyUp(adapter, actionObject, keyInputState);
        assertFalse(keyInputState.isAlt());
        assertTrue(keyInputState.isCtrl());
    }

    @Test
    public void shouldReturnNullIfKeyUpImplementationReturnsFalse() throws AppiumException {
        class TestAdapter extends DummyW3CActionAdapter {
            public boolean keyUp(KeyEvent keyEvent) {
                return false;
            }
        }
        TestAdapter testAdapter = new TestAdapter();
        String id = "keyboard";
        String value = "F";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(value);
        KeyInputState keyInputState = new KeyInputState();
        keyInputState.addPressed(value);
        assertNull(dispatchKeyUp(testAdapter, actionObject, keyInputState));
    }

    @Test
    public void shouldAddAndRemoveKeysFromKeyPressedState() throws AppiumException {
        DummyW3CActionAdapter adapter = new DummyW3CActionAdapter();
        String id = "keyboard";
        String value = "~";
        ActionObject actionObject = new ActionObject(id, KEY, KEY_DOWN, 0);
        actionObject.setValue(value);
        KeyInputState keyInputState = new KeyInputState();
        assertFalse(keyInputState.isPressed(value));

        // Press down a key and check that it's pressed
        dispatchKeyDown(adapter, actionObject, keyInputState);
        assertTrue(keyInputState.isPressed(value));

        // Release a key and check that it's not pressed
        dispatchKeyUp(adapter, actionObject, keyInputState);
        assertFalse(keyInputState.isPressed(value));
    }
}
