package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static io.appium.espressoserver.lib.helpers.w3c.processor.ActionsProcessor.processSourceActionSequence;
import static io.appium.espressoserver.lib.helpers.w3c.processor.KeyProcessor.processKeyAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processNullAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processPauseAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerMoveAction;
import static io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerUpOrDownAction;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessorTest {

    private InputSource pointerInputSource;

    @Before
    public void before() {
        pointerInputSource = new InputSource();
        pointerInputSource.setType(InputSourceType.POINTER);
    }


    @Test
    public void shouldRejectNullIfNotPause() {
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        try {
            processNullAction(action, InputSourceType.NONE, "any1", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("must be type 'pause'"));
        }
    }

    @Test
    public void shouldPassNullIfPause() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.PAUSE);
        ActionObject actionObject = processNullAction(action, InputSourceType.NONE, "any1", 0);
        assertEquals(actionObject.getType(), InputSourceType.NONE);
        assertEquals(actionObject.getSubType(), ActionType.PAUSE);
    }

    @Test
    public void shouldRejectPauseIfNegativeDuration() {
        Action action = new Action();
        action.setDuration(-1);
        try {
            processPauseAction(action, InputSourceType.NONE, "any3", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'duration' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldPassPauseWithNullDuration() throws InvalidArgumentException {
        Action action = new Action();
        assertNull(action.getDuration());
        ActionObject actionObject = processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertNull(actionObject.getDuration());
    }

    @Test
    public void shouldPassPauseWithDurationSet() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(10L);
        ActionObject actionObject = processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(actionObject.getDuration(), new Long(10));
    }

    @Test
    public void shouldRejectNullIfTypeNotPause() throws InvalidArgumentException {
        Action action = new Action();
        try {
            processPauseAction(action, InputSourceType.NONE, "any", 0);
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("type 'pause'"));
        }
    }

    @Test
    public void shouldProcessNullAsPauseAction() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(100);
        action.setType(ActionType.PAUSE);
        processPauseAction(action, InputSourceType.NONE, "any", 0);
        assertEquals(action.getDuration(), new Long(100));
    }

    @Test
    public void shouldRejectPointerMoveIfNegativeDuration() throws InvalidArgumentException {
        Action action = new Action();
        action.setDuration(-1);
        try {
            processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'duration' be greater than or equal to 0"));
        }
    }

    @Test
    public void shouldPassValidPointerMove() throws InvalidArgumentException {
        Action action = new Action();
        action.setX(100);
        action.setY(200);
        action.setDuration(300);
        ActionObject actionObject = processPointerMoveAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getX(), 100);
        assertEquals(actionObject.getY(), 200);
        assertEquals(actionObject.getDuration(), new Long(300));
    }

    @Test
    public void shouldRejectPointerUpOrDownIfButtonNegative() throws InvalidArgumentException {
        Action action = new Action();
        action.setButton(-100);
        try {
            processPointerUpOrDownAction(action, InputSourceType.POINTER, "any", 0);
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
            processKeyAction(action, InputSourceType.KEY, "any", 0);
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
        ActionObject actionObject = processKeyAction(action, InputSourceType.KEY, "any", 0);
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
            processKeyAction(action, InputSourceType.KEY, "any", 0);
            fail("expected exception was not occured.");
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("Must be a unicode point"));
        }
    }

    @Test
    public void shouldPassKeyIfUnicode() throws InvalidArgumentException {
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        String value = Character.toString('\uE9F0');
        action.setValue(value);
        ActionObject actionObject = processKeyAction(action, InputSourceType.POINTER, "any", 0);
        assertEquals(actionObject.getValue(), value);
        assertEquals(actionObject.getSubType(), ActionType.KEY_DOWN);
    }

    @Test
    public void shouldRejectInvalidPointerType() throws NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        try {
            processPointerAction(action, pointerInputSource, "any", 0);
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
        ActionObject actionObject = processPointerAction(action, pointerInputSource, "any", 0);
        assertEquals(actionObject.getDuration(), new Long(300));
        assertEquals(actionObject.getSubType(), ActionType.PAUSE);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerMove() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_MOVE);
        ActionObject actionObject = processPointerAction(action, pointerInputSource, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_MOVE);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerUp() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_UP);
        ActionObject actionObject = processPointerAction(action, pointerInputSource, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_UP);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldProcessPointerAsPointerDown() throws InvalidArgumentException, NotYetImplementedException {
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        ActionObject actionObject = processPointerAction(action, pointerInputSource, "any", 0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_DOWN);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }

    @Test
    public void shouldNotPassProcessorIfNoType() throws InvalidArgumentException, NotYetImplementedException {
        InputSource inputSource = new InputSource();
        try {
            processSourceActionSequence(inputSource, new ActiveInputSources(), new InputStateTable());
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'type' is required in input source"));
        }
    }

    @Test
    public void shouldNotPassProcessorIfNoId() throws InvalidArgumentException, NotYetImplementedException {
        InputSource inputSource = new InputSource();
        inputSource.setType(InputSourceType.KEY);
        try {
            processSourceActionSequence(inputSource, new ActiveInputSources(), new InputStateTable());
        } catch (InvalidArgumentException ie) {
            assertTrue(ie.getMessage().contains("'id' in action cannot be null"));
        }
    }

    @Test
    public void shouldCreateNewEntryInActiveStateTables() throws InvalidArgumentException, NotYetImplementedException {
        ActiveInputSources activeInputSources = new ActiveInputSources();
        InputStateTable inputStateTable = new InputStateTable();
        InputSource inputSource = new InputSource();
        inputSource.setActions(new ArrayList<Action>());
        inputSource.setType(InputSourceType.KEY);
        inputSource.setId("anything");
        assertFalse(activeInputSources.hasInputSource("anything"));
        processSourceActionSequence(inputSource, activeInputSources, inputStateTable);
        assertEquals(activeInputSources.getInputSource("anything"), inputSource);
        assertTrue(inputStateTable.getInputState("anything") instanceof KeyInputState);
    }

    @Test
    public void shouldReturnActionObjectsFromProcessor() throws InvalidArgumentException, NotYetImplementedException {
        ActiveInputSources activeInputSources = new ActiveInputSources();
        InputStateTable inputStateTable = new InputStateTable();
        InputSource inputSource = new InputSource();
        List<Action> actions = new ArrayList<>();
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        action.setButton(0);
        actions.add(action);
        inputSource.setActions(actions);
        inputSource.setType(InputSourceType.POINTER);
        inputSource.setId("anything");
        List<ActionObject> actionObjects = processSourceActionSequence(inputSource, activeInputSources, inputStateTable);
        assertEquals(actionObjects.size(), 1);
        ActionObject actionObject = actionObjects.get(0);
        assertEquals(actionObject.getSubType(), ActionType.POINTER_DOWN);
        assertEquals(actionObject.getType(), InputSourceType.POINTER);
    }
}
