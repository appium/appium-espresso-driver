package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;


import java.io.IOException;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionSequence;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;
import io.appium.espressoserver.lib.helpers.w3c.models.Tick;
import io.appium.espressoserver.lib.helpers.w3c.models.W3CActions;
import io.appium.espressoserver.test.assets.Helpers;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class ActionSequenceTest {

    @Test
    public void shouldTransposeActionsInW3CActions() throws IOException, InvalidArgumentException {
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions);

        // Tick #1 of 6
        Tick tick = actionSequence.next();
        Action action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(0));
        assertEquals(action.getX(), 100);
        assertEquals(action.getY(), 100);

        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(10));
        assertEquals(action.getX(), 200);
        assertEquals(action.getY(), 400);

        assertFalse(tick.hasNext());

        // Tick #2 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_DOWN);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_DOWN);
        assertEquals(action.getButton(), 0);

        // Tick #3 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), ActionType.PAUSE);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getType(), ActionType.PAUSE);
        assertEquals(action.getButton(), 0);

        assertFalse(tick.hasNext());

        // Tick #4 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(1000));
        assertTrue(action.isOriginPointer());
        assertEquals(action.getX(), 20);
        assertEquals(action.getY(), 0);

        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_MOVE);
        assertEquals(action.getDuration(), new Long(1000));
        assertTrue(action.isOriginPointer());
        assertEquals(action.getX(), 50);
        assertEquals(action.getY(), 0);

        assertFalse(tick.hasNext());

        // Tick #5 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_UP);
        assertEquals(action.getButton(), 0);

        action = tick.next();
        assertEquals(action.getType(), ActionType.POINTER_UP);
        assertEquals(action.getButton(), 0);

        assertFalse(tick.hasNext());

        // Tick #6 of 6
        tick = actionSequence.next();
        action = tick.next();
        assertEquals(action.getType(), ActionType.PAUSE);
        assertEquals(action.getDuration(), new Long(0));

        assertFalse(tick.hasNext());
        assertFalse(actionSequence.hasNext());
    }

}