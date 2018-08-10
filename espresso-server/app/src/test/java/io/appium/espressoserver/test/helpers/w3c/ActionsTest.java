package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter.PointerMoveEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.Actions;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;
import io.appium.espressoserver.test.assets.Helpers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ActionsTest {

    class AlteredDummyAdapter extends DummyW3CActionAdapter {
        @Override
        public long getViewportWidth() {
            // Bump up viewport width so we don't get out of bounds issues
            return 300;
        }
    }

    @Test
    public void shouldThrowIfAdapterNotSet() throws IOException, AppiumException {
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        Actions actions = Actions.class.cast((new Gson()).fromJson(multiTouchJson, Actions.class));

        try {
            actions.perform("123");
        } catch (AppiumException e) {
            assertTrue(e.getMessage().contains("Failed to initialize /actions adapter"));
        }

    }

    @Test
    public void shouldPerformPointerActionsOnASetOfInputSources() throws IOException, AppiumException {
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        Actions actions = Actions.class.cast((new Gson()).fromJson(multiTouchJson, Actions.class));
        actions.setAdapter(new AlteredDummyAdapter());

        String sessionId = "123";
        actions.perform(sessionId);
        InputStateTable inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId);
        PointerInputState finger1 = (PointerInputState) inputStateTable.getInputState("finger1");
        PointerInputState finger2 = (PointerInputState) inputStateTable.getInputState("finger2");

        // Check the state
        assertEquals(finger1.getX(), 120);
        assertEquals(finger1.getY(), 100);
        assertEquals(finger2.getX(), 250);
        assertEquals(finger2.getY(), 400);

        // Sanity check that it's recording pointer move events
        List<PointerMoveEvent> pointerMoveEvents = ((DummyW3CActionAdapter) actions.getAdapter()).getPointerMoveEvents();
        assertTrue(pointerMoveEvents.size() > 0);
    }

    @Test
    public void shouldPerformKeyActionsOnASetOfInputSources() throws IOException, AppiumException {
        String keyJson = Helpers.readAssetFile("key-actions.json");
        Actions actions = Actions.class.cast((new Gson()).fromJson(keyJson, Actions.class));
        actions.setAdapter(new AlteredDummyAdapter());

        String sessionId = "123";
        actions.perform(sessionId);
        InputStateTable inputStateTable = InputStateTable.getInputStateTableOfSession(sessionId);
        KeyInputState keyboard = (KeyInputState) inputStateTable.getInputState("keyboard");
        assertFalse(keyboard.isPressed("\\uE009"));
    }
}