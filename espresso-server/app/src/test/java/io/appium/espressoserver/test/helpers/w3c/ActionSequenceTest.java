package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionSequence;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.models.Tick;
import io.appium.espressoserver.lib.helpers.w3c.models.W3CActions;
import io.appium.espressoserver.test.assets.Helpers;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class ActionSequenceTest {

    @Test
    public void shouldTransposeActionsInW3CActions() throws IOException, InvalidArgumentException, NotYetImplementedException {
        String multiTouchJson = Helpers.readAssetFile("multi-touch-actions.json");
        W3CActions w3CActions = W3CActions.class.cast((new Gson()).fromJson(multiTouchJson, W3CActions.class));
        ActionSequence actionSequence = new ActionSequence(w3CActions);

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

}