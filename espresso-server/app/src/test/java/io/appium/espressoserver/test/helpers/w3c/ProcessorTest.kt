package io.appium.espressoserver.test.helpers.w3c


import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.state.ActiveInputSources
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState

import io.appium.espressoserver.lib.helpers.w3c.processor.ActionsProcessor.processSourceActionSequence
import io.appium.espressoserver.lib.helpers.w3c.processor.KeyProcessor.processKeyAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processNullAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PauseProcessor.processPauseAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerMoveAction
import io.appium.espressoserver.lib.helpers.w3c.processor.PointerProcessor.processPointerUpOrDownAction
import org.junit.Assert.*

class ProcessorTest {

    private var pointerInputSource: InputSource? = null

    @Before
    fun before() {
        pointerInputSource = InputSource()
        pointerInputSource!!.type = InputSourceType.POINTER
    }


    @Test
    fun shouldRejectNullIfNotPause() {
        val action = Action()
        action.type = ActionType.POINTER_DOWN
        try {
            processNullAction(action, InputSourceType.NONE, "any1", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("must be type 'pause'"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldPassNullIfPause() {
        val action = Action()
        action.type = ActionType.PAUSE
        val actionObject = processNullAction(action, InputSourceType.NONE, "any1", 0)
        assertEquals(actionObject.type, InputSourceType.NONE)
        assertEquals(actionObject.subType, ActionType.PAUSE)
    }

    @Test
    fun shouldRejectPauseIfNegativeDuration() {
        val action = Action()
        action.duration = -1f
        try {
            processPauseAction(action, InputSourceType.NONE, "any3", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("'duration' be greater than or equal to 0"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldPassPauseWithNullDuration() {
        val action = Action()
        assertNull(action.duration)
        val actionObject = processPauseAction(action, InputSourceType.NONE, "any", 0)
        assertNull(actionObject.duration)
    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldPassPauseWithDurationSet() {
        val action = Action()
        action.duration = 10f
        val actionObject = processPauseAction(action, InputSourceType.NONE, "any", 0)
        assertEquals(actionObject.duration, 10f)
    }

    @Test
    fun shouldRejectNullIfTypeNotPause() {
        val action = Action()
        try {
            processPauseAction(action, InputSourceType.NONE, "any", 0)
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("type 'pause'"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldProcessNullAsPauseAction() {
        val action = Action()
        action.duration = 100f
        action.type = ActionType.PAUSE
        processPauseAction(action, InputSourceType.NONE, "any", 0)
        assertEquals(action.duration, 100f)
    }

    @Test
    fun shouldRejectPointerMoveIfNegativeDuration() {
        val action = Action()
        action.duration = -1f
        try {
            processPointerMoveAction(action, InputSourceType.POINTER, "any", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("'duration' be greater than or equal to 0"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldPassValidPointerMove() {
        val action = Action()
        action.x = 100f
        action.y = 200f
        action.duration = 300f
        val actionObject = processPointerMoveAction(action, InputSourceType.POINTER, "any", 0)
        assertFloatEquals(actionObject.x!!, 100f)
        assertFloatEquals(actionObject.y!!, 200f)
        assertEquals(actionObject.duration, 300f)
    }

    @Test
    fun shouldRejectPointerUpOrDownIfButtonNegative() {
        val action = Action()
        action.button = -100
        try {
            processPointerUpOrDownAction(action, InputSourceType.POINTER, "any", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("property 'button' must be greater than or equal to 0"))
        }

    }

    @Test
    fun shouldRejectKeyIfNotValidType() {
        val action = Action()
        action.type = ActionType.POINTER_DOWN
        try {
            processKeyAction(action, InputSourceType.KEY, "any", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("has an invalid type"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldProcessKeyAsPauseIfPause() {
        val action = Action()
        action.type = ActionType.PAUSE
        action.duration = 300f
        val actionObject = processKeyAction(action, InputSourceType.KEY, "any", 0)
        assertEquals(actionObject.duration, 300f)
        assertEquals(actionObject.subType, ActionType.PAUSE)
        assertEquals(actionObject.type, InputSourceType.KEY)
    }

    @Test
    fun shouldRejectKeyIfNotUnicode() {
        val action = Action()
        action.type = ActionType.KEY_DOWN
        action.value = "asdfafsd"
        try {
            processKeyAction(action, InputSourceType.KEY, "any", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("Must be a unicode point"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun shouldPassKeyIfUnicode() {
        val action = Action()
        action.type = ActionType.KEY_DOWN
        val value = '\uE9F0'.toString()
        action.value = value
        val actionObject = processKeyAction(action, InputSourceType.POINTER, "any", 0)
        assertEquals(actionObject.value, value)
        assertEquals(actionObject.subType, ActionType.KEY_DOWN)
    }

    @Test
    @Throws(NotYetImplementedException::class)
    fun shouldRejectInvalidPointerType() {
        val action = Action()
        action.type = ActionType.KEY_DOWN
        try {
            processPointerAction(action, pointerInputSource!!, "any", 0)
            fail("expected exception was not occured.")
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("has an invalid type"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldProcessPointerAsPauseIfPause() {
        val action = Action()
        action.type = ActionType.PAUSE
        action.duration = 300f
        val actionObject = processPointerAction(action, pointerInputSource!!, "any", 0)
        assertEquals(actionObject.duration, 300f)
        assertEquals(actionObject.subType, ActionType.PAUSE)
        assertEquals(actionObject.type, InputSourceType.POINTER)
    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldProcessPointerAsPointerMove() {
        val action = Action()
        action.type = ActionType.POINTER_MOVE
        val actionObject = processPointerAction(action, pointerInputSource!!, "any", 0)
        assertEquals(actionObject.subType, ActionType.POINTER_MOVE)
        assertEquals(actionObject.type, InputSourceType.POINTER)
    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldProcessPointerAsPointerUp() {
        val action = Action()
        action.type = ActionType.POINTER_UP
        val actionObject = processPointerAction(action, pointerInputSource!!, "any", 0)
        assertEquals(actionObject.subType, ActionType.POINTER_UP)
        assertEquals(actionObject.type, InputSourceType.POINTER)
    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldProcessPointerAsPointerDown() {
        val action = Action()
        action.type = ActionType.POINTER_DOWN
        val actionObject = processPointerAction(action, pointerInputSource!!, "any", 0)
        assertEquals(actionObject.subType, ActionType.POINTER_DOWN)
        assertEquals(actionObject.type, InputSourceType.POINTER)
    }

    @Test
    @Throws(NotYetImplementedException::class)
    fun shouldNotPassProcessorIfNoType() {
        val inputSource = InputSource()
        try {
            processSourceActionSequence(inputSource, ActiveInputSources(), InputStateTable())
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("'type' is required in input source"))
        }

    }

    @Test
    @Throws(NotYetImplementedException::class)
    fun shouldNotPassProcessorIfNoId() {
        val inputSource = InputSource()
        inputSource.type = InputSourceType.KEY
        try {
            processSourceActionSequence(inputSource, ActiveInputSources(), InputStateTable())
        } catch (ie: InvalidArgumentException) {
            assertTrue(ie.message!!.contains("'id' in action cannot be null"))
        }

    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldCreateNewEntryInActiveStateTables() {
        val activeInputSources = ActiveInputSources()
        val inputStateTable = InputStateTable()
        val inputSource = InputSource()
        inputSource.actions = ArrayList()
        inputSource.type = InputSourceType.KEY
        inputSource.id = "anything"
        assertFalse(activeInputSources.hasInputSource("anything"))
        processSourceActionSequence(inputSource, activeInputSources, inputStateTable)
        assertEquals(activeInputSources.getInputSource("anything"), inputSource)
        assertTrue(inputStateTable.getInputState("anything") is KeyInputState)
    }

    @Test
    @Throws(InvalidArgumentException::class, NotYetImplementedException::class)
    fun shouldReturnActionObjectsFromProcessor() {
        val activeInputSources = ActiveInputSources()
        val inputStateTable = InputStateTable()
        val inputSource = InputSource()
        val actions = ArrayList<Action>()
        val action = Action()
        action.type = ActionType.POINTER_DOWN
        action.button = 0
        actions.add(action)
        inputSource.actions = actions
        inputSource.type = InputSourceType.POINTER
        inputSource.id = "anything"
        val actionObjects = processSourceActionSequence(inputSource, activeInputSources, inputStateTable)
        assertEquals(actionObjects.size.toLong(), 1)
        val actionObject = actionObjects[0]
        assertEquals(actionObject.subType, ActionType.POINTER_DOWN)
        assertEquals(actionObject.type, InputSourceType.POINTER)
    }
}
