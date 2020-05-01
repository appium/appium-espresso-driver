package io.appium.espressoserver.test.helpers.w3c

import com.google.gson.Gson
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.models.Origin
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class InputSourceTest {
    @Test
    fun shouldDeserializeInputSource() {
        val postJson = "{\"id\":\"something1\"}"
        val inputSource = Gson().fromJson(postJson, InputSource::class.java)
        Assert.assertEquals(inputSource.id, "something1")
    }

    @Test
    fun shouldDeserializePointerInputSource() {
        val postJson = "{\"type\":\"pointer\",\"id\":\"something2\", \"parameters\": {\"pointerType\": \"touch\"}}"
        val inputSource = Gson().fromJson(postJson, InputSource::class.java)
        Assert.assertEquals(inputSource.id, "something2")
        Assert.assertEquals(inputSource.pointerType, InputSource.PointerType.TOUCH)
    }

    @Test
    fun shouldDeserializeElementOriginPointer() {
        val postJson = "{\"type\":\"pointer\",\"id\":\"something2\", \"parameters\": {\"pointerType\": \"touch\"}, " +
                "\"actions\": [{\"type\":\"pointerMove\",\"duration\":1000,\"origin\":{\"element-6066-11e4-a52e-4f735466cecf\":\"some-element-id\"},\"x\":50,\"y\":0}]}"
        val inputSource = Gson().fromJson(postJson, InputSource::class.java)
        Assert.assertEquals(inputSource.id, "something2")
        Assert.assertEquals(inputSource.pointerType, InputSource.PointerType.TOUCH)
        val action = inputSource.actions!![0]
        Assert.assertEquals(action.origin.type, Origin.ELEMENT)
        Assert.assertEquals(action.origin.elementId, "some-element-id")
    }

    @Test
    fun shouldDeserializeComplexPointerObject() {
        val postJson = "{\"type\":\"pointer\",\"id\":\"finger1\",\"parameters\":{\"pointerType\":\"touch\"},\"actions\":[{\"type\":\"pointerMove\",\"duration\":0,\"x\":100,\"y\":200},{\"type\":\"pointerDown\",\"button\":0},{\"type\":\"pause\",\"duration\":500},{\"type\":\"pointerMove\",\"duration\":1000,\"origin\":\"pointer\",\"x\":50,\"y\":10},{\"type\":\"pointerUp\",\"button\":0}]}"
        val inputSource = Gson().fromJson(postJson, InputSource::class.java)
        Assert.assertEquals(inputSource.type, InputSourceType.POINTER)
        Assert.assertEquals(inputSource.id, "finger1")
        Assert.assertEquals(inputSource.pointerType, InputSource.PointerType.TOUCH)
        val actions = inputSource.actions
        val actionOne = actions!![0]
        Assert.assertEquals(actionOne.type, InputSource.ActionType.POINTER_MOVE)
        assertFloatEquals(actionOne.duration!!, 0f)
        assertFloatEquals(actionOne.x!!, 100f)
        assertFloatEquals(actionOne.y!!, 200f)
        val actionTwo = actions[1]
        Assert.assertEquals(actionTwo.type, InputSource.ActionType.POINTER_DOWN)
        assertEquals(actionTwo.button, 0)
        val actionThree = actions[2]
        Assert.assertEquals(actionThree.type, InputSource.ActionType.PAUSE)
        assertFloatEquals(actionThree.duration!!, 500f)
        val actionFour = actions[3]
        Assert.assertEquals(actionFour.type, InputSource.ActionType.POINTER_MOVE)
        assertFloatEquals(actionFour.duration!!, 1000f)
        Assert.assertEquals(actionFour.origin.type, "pointer")
        Assert.assertTrue(actionFour.isOriginPointer)
        assertFloatEquals(actionFour.x!!, 50f)
        assertFloatEquals(actionFour.y!!, 10f)
        val actionFive = actions[4]
        Assert.assertEquals(actionFive.type, InputSource.ActionType.POINTER_UP)
        assertEquals(actionFive.button, 0)
    }

    @Test
    fun shouldDeserializeComplexKeyObject() {
        val postJson = "{\"type\":\"key\",\"id\":\"keyboard\",\"actions\":[{\"type\":\"keyDown\",\"value\":\"key1\"},{\"type\":\"keyDown\",\"value\":\"key2\"},{\"type\":\"keyUp\",\"value\":\"key1\"},{\"type\":\"keyUp\",\"value\":\"key2\"}]}"
        val inputSource = Gson().fromJson(postJson, InputSource::class.java)
        Assert.assertEquals(inputSource.type, InputSourceType.KEY)
        Assert.assertEquals(inputSource.id, "keyboard")
        Assert.assertNull(inputSource.pointerType)
        val actions = inputSource.actions
        val actionOne = actions!![0]
        Assert.assertEquals(actionOne.type, InputSource.ActionType.KEY_DOWN)
        Assert.assertEquals(actionOne.value, "key1")
        val actionTwo = actions[1]
        Assert.assertEquals(actionTwo.type, InputSource.ActionType.KEY_DOWN)
        Assert.assertEquals(actionTwo.value, "key2")
        val actionThree = actions[2]
        Assert.assertEquals(actionThree.type, InputSource.ActionType.KEY_UP)
        Assert.assertEquals(actionThree.value, "key1")
        val actionFour = actions[3]
        Assert.assertEquals(actionFour.type, InputSource.ActionType.KEY_UP)
        Assert.assertEquals(actionFour.value, "key2")
    }

    @Test
    fun shouldDefaultPointerTypeToTouch() {
        val inputSource = InputSource()
        inputSource.type = InputSourceType.POINTER
        Assert.assertEquals(inputSource.pointerType, InputSource.PointerType.TOUCH)
    }

    @Test
    fun shouldMapKeyObjectToKeyInputState() {
        val actions: MutableList<InputSource.Action> = ArrayList()
        val action = InputSource.Action()
        action.type = InputSource.ActionType.KEY_DOWN
        actions.add(action)
        val inputSource = InputSource(InputSourceType.KEY, "any", null, actions)

        // Check the initial state
        val inputState = inputSource.defaultState as KeyInputState?
        Assert.assertFalse(inputState!!.isAlt)
        Assert.assertFalse(inputState.isCtrl)
        Assert.assertFalse(inputState.isMeta)
        Assert.assertFalse(inputState.isShift)

        // Add and remove a key and check the state along the way
        Assert.assertFalse(inputState.isPressed("a"))
        inputState.addPressed("a")
        Assert.assertTrue(inputState.isPressed("a"))
        inputState.removePressed("a")
        Assert.assertFalse(inputState.isPressed("a"))
    }

    @Test
    fun shouldMapPointerObjectToPointerInputState() {
        val actions: MutableList<InputSource.Action> = ArrayList()
        val action = InputSource.Action()
        action.type = InputSource.ActionType.POINTER_DOWN
        actions.add(action)
        val parameters = InputSource.Parameters()
        parameters.pointerType = InputSource.PointerType.TOUCH
        val inputSource = InputSource(InputSourceType.POINTER, "any", parameters, actions)

        // Check the initial state
        val inputState = inputSource.defaultState as PointerInputState?
        Assert.assertFalse(inputState!!.isPressed(1))
        inputState.addPressed(1)
        Assert.assertTrue(inputState.isPressed(1))
        inputState.removePressed(1)
        Assert.assertFalse(inputState.isPressed(1))
    }

    @Test
    fun shouldMapNullInputSourceStateToNull() {
        val actions: MutableList<InputSource.Action> = ArrayList()
        val action = InputSource.Action()
        action.type = InputSource.ActionType.POINTER_DOWN
        actions.add(action)
        val inputSource = InputSource(InputSourceType.NONE, "any", null, actions)
        Assert.assertNull(inputSource.defaultState)
    }
}