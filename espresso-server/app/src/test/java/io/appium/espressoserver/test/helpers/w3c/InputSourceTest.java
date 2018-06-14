package io.appium.espressoserver.test.helpers.w3c;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Parameters;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action.ELEMENT_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class InputSourceTest {

    @Test
    public void shouldDeserializeInputSource() {
        String postJson = "{\"id\":\"something1\"}";
        InputSource inputSource = InputSource.class.cast((new Gson()).fromJson(postJson, InputSource.class));
        assertEquals(inputSource.getId(), "something1");
    }

    @Test
    public void shouldDeserializePointerInputSource() {
        String postJson = "{\"type\":\"pointer\",\"id\":\"something2\", \"parameters\": {\"pointerType\": \"touch\"}}";
        InputSource inputSource = InputSource.class.cast((new Gson()).fromJson(postJson, InputSource.class));
        assertEquals(inputSource.getId(), "something2");
        assertEquals(inputSource.getPointerType(), InputSource.PointerType.TOUCH);
    }

    @Test
    public void shouldDeserializeElementOriginPointer() {
        String postJson = "{\"type\":\"pointer\",\"id\":\"something2\", \"parameters\": {\"pointerType\": \"touch\"}, " +
                "\"actions\": [{\"type\":\"pointerMove\",\"duration\":1000,\"origin\":{\"element-6066-11e4-a52e-4f735466cecf\":\"some-element-id\"},\"x\":50,\"y\":0}]}";
        InputSource inputSource = InputSource.class.cast((new Gson()).fromJson(postJson, InputSource.class));
        assertEquals(inputSource.getId(), "something2");
        assertEquals(inputSource.getPointerType(), InputSource.PointerType.TOUCH);
        Action action = inputSource.getActions().get(0);
        assertEquals(action.getOrigin().getType(), ELEMENT_CODE);
        assertEquals(action.getOrigin().getElementId(), "some-element-id");
    }

    @Test
    public void shouldDeserializeComplexPointerObject() {
        String postJson = "{\"type\":\"pointer\",\"id\":\"finger1\",\"parameters\":{\"pointerType\":\"touch\"},\"actions\":[{\"type\":\"pointerMove\",\"duration\":0,\"x\":100,\"y\":200},{\"type\":\"pointerDown\",\"button\":0},{\"type\":\"pause\",\"duration\":500},{\"type\":\"pointerMove\",\"duration\":1000,\"origin\":\"pointer\",\"x\":50,\"y\":10},{\"type\":\"pointerUp\",\"button\":0}]}";
        InputSource inputSource = InputSource.class.cast((new Gson()).fromJson(postJson, InputSource.class));
        assertEquals(inputSource.getType(), InputSourceType.POINTER);
        assertEquals(inputSource.getId(), "finger1");
        assertEquals(inputSource.getPointerType(), PointerType.TOUCH);

        List<Action> actions = inputSource.getActions();
        Action actionOne = actions.get(0);
        assertEquals(actionOne.getType(), ActionType.POINTER_MOVE);
        assertEquals(actionOne.getDuration(), new Long(0));
        assertEquals(actionOne.getX(), new Long(100));
        assertEquals(actionOne.getY(), new Long(200));

        Action actionTwo = actions.get(1);
        assertEquals(actionTwo.getType(), ActionType.POINTER_DOWN);
        assertEquals(actionTwo.getButton(), 0);

        Action actionThree = actions.get(2);
        assertEquals(actionThree.getType(), ActionType.PAUSE);
        assertEquals(actionThree.getDuration(), new Long(500));

        Action actionFour = actions.get(3);
        assertEquals(actionFour.getType(), ActionType.POINTER_MOVE);
        assertEquals(actionFour.getDuration(), new Long(1000));
        assertEquals(actionFour.getOrigin().getType(), "pointer");
        assertTrue(actionFour.isOriginPointer());
        assertEquals(actionFour.getX(), new Long(50));
        assertEquals(actionFour.getY(), new Long(10));

        Action actionFive = actions.get(4);
        assertEquals(actionFive.getType(), ActionType.POINTER_UP);
        assertEquals(actionFive.getButton(), 0);
    }

    @Test
    public void shouldDeserializeComplexKeyObject() {
        String postJson = "{\"type\":\"key\",\"id\":\"keyboard\",\"actions\":[{\"type\":\"keyDown\",\"value\":\"key1\"},{\"type\":\"keyDown\",\"value\":\"key2\"},{\"type\":\"keyUp\",\"value\":\"key1\"},{\"type\":\"keyUp\",\"value\":\"key2\"}]}";
        InputSource inputSource = InputSource.class.cast((new Gson()).fromJson(postJson, InputSource.class));
        assertEquals(inputSource.getType(), InputSourceType.KEY);
        assertEquals(inputSource.getId(), "keyboard");
        assertNull(inputSource.getPointerType());

        List<Action> actions = inputSource.getActions();
        Action actionOne = actions.get(0);
        assertEquals(actionOne.getType(), ActionType.KEY_DOWN);
        assertEquals(actionOne.getValue(), "key1");

        Action actionTwo = actions.get(1);
        assertEquals(actionTwo.getType(), ActionType.KEY_DOWN);
        assertEquals(actionTwo.getValue(), "key2");

        Action actionThree = actions.get(2);
        assertEquals(actionThree.getType(), ActionType.KEY_UP);
        assertEquals(actionThree.getValue(), "key1");

        Action actionFour = actions.get(3);
        assertEquals(actionFour.getType(), ActionType.KEY_UP);
        assertEquals(actionFour.getValue(), "key2");
    }

    @Test
    public void shouldDefaultPointerTypeToTouch() {
        InputSource inputSource = new InputSource();
        inputSource.setType(InputSourceType.POINTER);
        assertEquals(inputSource.getPointerType(), PointerType.TOUCH);
    }

    @Test
    public void shouldMapKeyObjectToKeyInputState() {
        List<Action> actions = new ArrayList<>();
        Action action = new Action();
        action.setType(ActionType.KEY_DOWN);
        actions.add(action);
        InputSource inputSource = new InputSource(InputSourceType.KEY, "any", null, actions);

        // Check the initial state
        KeyInputState inputState = (KeyInputState) inputSource.getDefaultState();
        assertTrue(!inputState.isAlt());
        assertTrue(!inputState.isCtrl());
        assertTrue(!inputState.isMeta());
        assertTrue(!inputState.isShift());

        // Add and remove a key and check the state along the way
        assertTrue(!inputState.isPressed("a"));
        inputState.addPressed("a");
        assertTrue(inputState.isPressed("a"));
        inputState.removePressed("a");
        assertTrue(!inputState.isPressed("a"));
    }

    @Test
    public void shouldMapPointerObjectToPointerInputState() {
        List<Action> actions = new ArrayList<>();
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        actions.add(action);
        Parameters parameters = new Parameters();
        parameters.setPointerType(PointerType.TOUCH);
        InputSource inputSource = new InputSource(InputSourceType.POINTER, "any", parameters, actions);

        // Check the initial state
        PointerInputState inputState = (PointerInputState) inputSource.getDefaultState();
        assertTrue(!inputState.isPressed(1));
        inputState.addPressed(1);
        assertTrue(inputState.isPressed(1));
        inputState.removePressed(1);
        assertTrue(!inputState.isPressed(1));
    }

    @Test
    public void shouldMapNullInputSourceStateToNull() {
        List<Action> actions = new ArrayList<>();
        Action action = new Action();
        action.setType(ActionType.POINTER_DOWN);
        actions.add(action);
        InputSource inputSource = new InputSource(InputSourceType.NONE, "any", null, actions);
        assertNull(inputSource.getDefaultState());
    }
}