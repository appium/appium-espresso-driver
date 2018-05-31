package io.appium.espressoserver.test.helpers.w3c;


import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;
import io.appium.espressoserver.lib.helpers.w3c.models.W3CActions;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class W3CActionsTest {

    @Test
    public void shouldRejectPauseActionsIfDurationInvalid() {
        Action action = new Action();
        action.setDuration(-1);
        try {
            W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("integer > 0"));
        }
    }

    @Test
    public void shouldPassPauseWithNullDuration() throws InvalidArgumentException {
        Action action = new Action();
        assertNull(action.getDuration());
        ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(actionObject.getDuration(), 0L);
    }

    @Test
    public void shouldPassPauseWithDurationSet() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(10L);
        ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(actionObject.getDuration(), 10L);
    }

    @Test
    public void shouldRejectNullIfTypeNotPause() throws InvalidArgumentException {
        Action action = new Action();
        try {
            ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("type 'pause'"));
        }
    }

    @Test
    public void shouldProcessNullAsPauseAction() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(100);
        action.setType(ActionType.PAUSE);
        ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(action.getDuration(), new Long(100));
    }


}
