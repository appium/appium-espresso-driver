package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionSequence;
import io.appium.espressoserver.lib.helpers.w3c.models.ActiveInputSources;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.models.Tick;
import io.appium.espressoserver.lib.helpers.w3c.models.W3CActions;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;
import io.appium.espressoserver.test.assets.Helpers;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class ActionSequenceTest {

    @Test
    public void shouldPullOutPointerActionsInW3CActions() throws IOException, InvalidArgumentException, NotYetImplementedException {
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions, new ActiveInputSources(), new InputStateTable());

        // Tick #1 of 6
        Tick tick = actionSequence.next();
        ActionObject action = tick.next();
        assertEquals(action.getId(), "finger1");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(0));
        assertEquals(action.getX(), new Long(100));
        assertEquals(action.getY(), new Long(100));

        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(10));
        assertEquals(action.getX(), new Long(200));
        assertEquals(action.getY(), new Long(400));

        assertFalse(tick.hasNext());

        // Tick #2 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getId(), "finger1");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_DOWN);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_DOWN);
        assertEquals(action.getButton(), 0);

        // Tick #3 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getId(), "finger1");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.PAUSE);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.PAUSE);
        assertEquals(action.getButton(), 0);

        assertFalse(tick.hasNext());

        // Tick #4 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getId(), "finger1");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(1000));
        assertTrue(action.getPointer().equals(PointerType.TOUCH));
        assertEquals(action.getX(), new Long(20));
        assertEquals(action.getY(), new Long(0));

        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(1000));
        assertEquals(action.getOrigin(), InputSource.POINTER);
        assertEquals(action.getX(), new Long(50));
        assertEquals(action.getY(), new Long(0));

        assertFalse(tick.hasNext());

        // Tick #5 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getId(), "finger1");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_UP);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.POINTER_UP);
        assertEquals(action.getButton(), 0);

        assertFalse(tick.hasNext());

        // Tick #6 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getId(), "finger2");
        assertEquals(action.getType(), InputSourceType.POINTER);
        assertEquals(action.getSubType(), ActionType.PAUSE);
        assertEquals(action.getDuration(), new Long(0));

        assertFalse(tick.hasNext());
        assertFalse(actionSequence.hasNext());
    }


    @Test
    public void shouldPullOutKeyActionsInW3CActions() throws IOException, InvalidArgumentException, NotYetImplementedException {
        String multiTouchJson = Helpers.readAssetFile("key-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions, new ActiveInputSources(), new InputStateTable());

        ActionObject action;
        Tick tick;

        String unicodeChar = Character.toString('\uE009');

        // Tick #1 of 5
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), InputSourceType.KEY);
        assertEquals(action.getSubType(), ActionType.KEY_DOWN);
        assertEquals(action.getValue(), unicodeChar);
        assertEquals(action.getId(), "keyboard");
        assertFalse(tick.hasNext());

        // Tick #2 of 5
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), InputSourceType.KEY);
        assertEquals(action.getSubType(), ActionType.PAUSE);
        assertEquals(action.getId(), "keyboard");
        assertFalse(tick.hasNext());

        // Tick #3 of 5
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), InputSourceType.KEY);
        assertEquals(action.getSubType(), ActionType.KEY_DOWN);
        assertEquals(action.getId(), "keyboard");
        assertFalse(tick.hasNext());

        // Tick #4 of 5
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), InputSourceType.KEY);
        assertEquals(action.getSubType(), ActionType.KEY_UP);
        assertEquals(action.getId(), "keyboard");
        assertFalse(tick.hasNext());

        // Tick #5 of 5
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), InputSourceType.KEY);
        assertEquals(action.getSubType(), ActionType.KEY_UP);
        assertEquals(action.getId(), "keyboard");
        assertFalse(tick.hasNext());

        assertFalse(actionSequence.hasNext());
    }

    @Test
    public void shouldDispatchW3CPointerActions() throws IOException, AppiumException, InterruptedException, ExecutionException {
        class AlteredDummyAdapter extends DummyW3CActionAdapter {
            @Override
            public long getViewportWidth() {
                // Bump up viewport width so we don't get out of bounds issues
                return 300;
            }
        }

        InputStateTable inputStateTable = new InputStateTable();
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions, new ActiveInputSources(), new InputStateTable());

        long timeBefore = System.currentTimeMillis();
        actionSequence.dispatch(new AlteredDummyAdapter(), inputStateTable);
        long elapsedTime = System.currentTimeMillis() - timeBefore;

        // Must be greater than 2s because that's the total duration
        assertTrue(elapsedTime >= 2000);

        // Check that it's under 2.5s though to verify that it's not TOO long
        assertTrue(elapsedTime < 2100);

        PointerInputState finger1State = (PointerInputState) inputStateTable.getInputState("finger1");
        PointerInputState finger2State = (PointerInputState) inputStateTable.getInputState("finger2");

        assertEquals(finger1State.getX(), 120);
        assertEquals(finger1State.getY(), 100);
        assertEquals(finger2State.getX(), 250);
        assertEquals(finger2State.getY(), 400);
    }

    @Test
    public void shouldDispatchW3CKeyActions() throws IOException, AppiumException, InterruptedException, ExecutionException {
        InputStateTable inputStateTable = new InputStateTable();
        String multiTouchJson = Helpers.readAssetFile("key-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions, new ActiveInputSources(), new InputStateTable());

        long timeBefore = System.currentTimeMillis();
        actionSequence.dispatch(new DummyW3CActionAdapter(), inputStateTable);
        long elapsedTime = System.currentTimeMillis() - timeBefore;
        System.out.println(elapsedTime);
        assertTrue(elapsedTime >= 500);
        assertTrue(elapsedTime <= 600);

    }

}