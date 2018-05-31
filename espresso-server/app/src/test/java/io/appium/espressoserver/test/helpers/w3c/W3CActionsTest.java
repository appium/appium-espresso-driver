package io.appium.espressoserver.test.helpers.w3c;


import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.appium.espressoserver.lib.handlers.NotYetImplemented;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
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
    public void shouldRejectNullIfNotPause() {
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        try {
            W3CActions.processNullAction(action, InputSourceType.NONE, "any1", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("must be type 'pause'"));
        }
    }

    @Test
    public void shouldPassNullIfPause() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.PAUSE);
        ActionObject actionObject = W3CActions.processNullAction(action, InputSourceType.NONE, "any1", 0);
        assertEquals(actionObject.getType(), InputSourceType.NONE);
        assertEquals(actionObject.getSubType(), ActionType.PAUSE);
    }

    @Test
    public void shouldRejectPauseIfNegativeDuration() {
        Action action = new Action();
        action.setDuration(-1);
        try {
            W3CActions.processPauseAction(action, InputSourceType.NONE, "any3", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'duration' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldPassPauseWithNullDuration() throws InvalidArgumentException {
        Action action = new Action();
        assertNull(action.getDuration());
        ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertNull(actionObject.getDuration());
    }

    @Test
    public void shouldPassPauseWithDurationSet() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(10L);
        ActionObject actionObject = W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(actionObject.getDuration(), new Long(10));
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
        W3CActions.processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(action.getDuration(), new Long(100));
    }

    @Test
    public void shouldRejectPointerMoveIfNegativeDuration() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(-1);
        try {
            W3CActions.processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'duration' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldRejectPointerMoveIfNegativeX() throws InvalidArgumentException {
        Action action = new Action();
        action.setX(-100);
        try {
            W3CActions.processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'x' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldRejectPointerMoveIfNegativeY() throws InvalidArgumentException {
        Action action = new Action();
        action.setY(-100);
        try {
            W3CActions.processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'y' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldPassValidPointerMove() throws InvalidArgumentException {
        Action action = new Action();
        action.setX(100);
        action.setY(200);
        action.setDuration(300);
        ActionObject actionObject = W3CActions.processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getX(), new Long(100));
        assertEquals(actionObject.getY(), new Long(200));
        assertEquals(actionObject.getDuration(), new Long(300));
    }

    @Test
    public void shouldRejectPointerUpOrDownIfButtonNegative() throws InvalidArgumentException {
        Action action = new Action();
        action.setButton(-100);
        try {
            W3CActions.processPointerUpOrDownAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("property 'button' must be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldRejectKeyIfNotValidType() {
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        try {
            W3CActions.processKeyAction(action, InputSourceType.KEY, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("has an invalid type"));
        }
    }

    @Test
    public void shouldProcessKeyAsPauseIfPause() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.PAUSE);
        action.setDuration(300);
        ActionObject actionObject = W3CActions.processKeyAction(action, InputSourceType.KEY, "any", 0);
        assertEquals(actionObject.getDuration(), new Long(300));
        assertEquals(actionObject.getSubType(), ActionType.PAUSE);
        assertEquals(actionObject.getType(), InputSourceType.KEY);
    }

    @Test
    public void shouldRejectKeyIfNotUnicode() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        action.setValue("asdfafsd");
        try {
            W3CActions.processKeyAction(action, InputSourceType.KEY, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("Must be a unicode point"));
        }
    }

    @Test
    public void shouldPassKeyIfUnicode() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        action.setValue("\\uE9F0");
        ActionObject actionObject = W3CActions.processKeyAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getValue(), "\\uE9F0");
        assertEquals(actionObject.getSubType(), ActionType.KEY_DOWN);
    }

    @Test
    public void shouldRejectInvalidPointerType() throws NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        try {
            W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("has an invalid type"));
        }
    }

    @Test
    public void shouldProcessPointerAsPauseIfPause() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.PAUSE);
        action.setDuration(300);
        ActionObject actionObject = W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getDuration(), new Long(300));
        assertEquals(actionObject.getSubType(), ActionType.PAUSE);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerMove() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_MOVE);
        ActionObject actionObject = W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerUp() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_UP);
        ActionObject actionObject = W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_UP);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerDown() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        ActionObject actionObject = W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_DOWN);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldRejectPointerCancel() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.POINTER_CANCEL);
        try {
            W3CActions.processPointerAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (NotYetImplementedException ie) {
            assertTrue(ie.getMessage().contains("not yet implemented"));
        }
    }
}
