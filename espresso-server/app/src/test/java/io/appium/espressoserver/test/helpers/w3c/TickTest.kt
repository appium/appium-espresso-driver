package io.appium.espressoserver.test.helpers.w3c

import org.junit.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.models.Origin
import io.appium.espressoserver.lib.helpers.w3c.models.Tick
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.NONE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

class TickTest {

    @Test
    fun `should calculate max duration as zero if no durations`() {
        val tick = Tick()
        tick.addAction(ActionObject())
        assertFloatEquals(tick.calculateTickDuration(), 0f)
    }

    @Test
    fun `should calculate the max duration`() {

        val valueOne = arrayOf(10F, 30F, 0F)
        val valueTwo = arrayOf(20F, 10F, 1F)
        val expectedMax = arrayOf(20F, 30F, 1F)

        for (i in valueOne.indices) {
            val tick = Tick()
            val actionObjectOne = ActionObject()
            actionObjectOne.type = NONE
            actionObjectOne.subType = PAUSE
            actionObjectOne.duration = valueOne[i]

            val actionObjectTwo = ActionObject()
            actionObjectTwo.type = POINTER
            actionObjectTwo.subType = POINTER_MOVE
            actionObjectTwo.duration = valueTwo[i]

            val actionObjectThree = ActionObject()
            actionObjectTwo.type = POINTER
            actionObjectTwo.subType = POINTER_MOVE

            tick.addAction(actionObjectOne)
            tick.addAction(actionObjectTwo)
            tick.addAction(actionObjectThree)

            assertFloatEquals(tick.calculateTickDuration(), expectedMax[i])
        }
    }

    @Test
    @Throws(AppiumException::class)
    fun `should add key to key input state when running key actions`() {
        val inputStateTable = InputStateTable()
        val tick = Tick()
        val sourceId = "something1"
        val actionObject = ActionObject(sourceId, KEY, null, 0)
        actionObject.subType = KEY_DOWN
        actionObject.value = "F"
        tick.addAction(actionObject)
        assertFalse(inputStateTable.hasInputState(sourceId))
        tick.dispatchAll(DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration())
        assertTrue(inputStateTable.getInputState(sourceId) is KeyInputState)
    }

    @Test
    @Throws(AppiumException::class)
    fun `should add pointer input state when running pointer actions`() {
        val inputStateTable = InputStateTable()
        val tick = Tick()
        val sourceId = "something2"
        val actionObject = ActionObject(sourceId, POINTER, null, 0)
        actionObject.subType = POINTER_DOWN
        actionObject.button = 0
        tick.addAction(actionObject)
        assertFalse(inputStateTable.hasInputState(sourceId))
        tick.dispatchAll(DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration())
        assertTrue(inputStateTable.getInputState(sourceId) is PointerInputState)
    }

    @Test
    @Throws(AppiumException::class)
    fun `should not affect input state when NONE event is dispatched`() {
        val inputStateTable = InputStateTable()
        val tick = Tick()
        val sourceId = "something3"
        val actionObject = ActionObject(sourceId, NONE, null, 0)
        tick.addAction(actionObject)
        assertFalse(inputStateTable.hasInputState(sourceId))
        tick.dispatchAll(DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration())
        assertFalse(inputStateTable.hasInputState(sourceId))
    }

    @Test
    @Throws(AppiumException::class)
    fun `should dispatch a sequence of key events`() {
        val inputStateTable = InputStateTable()
        val tick = Tick()
        val sourceId = "something4"
        var keyInputState = KeyInputState()
        keyInputState.addPressed("g")
        inputStateTable.addInputState(sourceId, keyInputState)
        val actionObjectOne = ActionObject(sourceId, KEY, null, 0)
        actionObjectOne.subType = KEY_DOWN
        actionObjectOne.value = "e"

        val actionObjectTwo = ActionObject(sourceId, KEY, null, 0)
        actionObjectTwo.subType = KEY_DOWN
        actionObjectTwo.value = "f"

        val actionObjectThree = ActionObject(sourceId, KEY, null, 0)
        actionObjectThree.subType = KEY_UP
        actionObjectThree.value = "g"

        val actionObjectFour = ActionObject(sourceId, KEY, null, 0)
        actionObjectFour.subType = KEY_DOWN
        actionObjectFour.value = "\uE008"

        tick.addAction(actionObjectOne)
        tick.addAction(actionObjectTwo)
        tick.addAction(actionObjectThree)
        tick.addAction(actionObjectFour)

        keyInputState = inputStateTable.getInputState(sourceId) as KeyInputState
        assertFalse(keyInputState.isPressed("e"))
        assertFalse(keyInputState.isPressed("f"))
        assertTrue(keyInputState.isPressed("g"))
        assertFalse(keyInputState.isShift)

        tick.dispatchAll(DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration())
        assertTrue(keyInputState.isPressed("e"))
        assertTrue(keyInputState.isPressed("f"))
        assertFalse(keyInputState.isPressed("g"))
        assertTrue(keyInputState.isShift)
    }

    @Test
    @Throws(AppiumException::class, InterruptedException::class, ExecutionException::class)
    fun `should dispatch a series of pointer move events`() {
        val inputStateTable = InputStateTable()
        val keyInputState = KeyInputState()
        keyInputState.isShift = true
        inputStateTable.addInputState("keyInputs", keyInputState)
        val tick = Tick()

        val sourceId = "something4"
        val sourceId2 = "something5"
        val pointerInputState = PointerInputState(TOUCH)
        pointerInputState.type = TOUCH
        pointerInputState.x = 5f
        pointerInputState.y = 6f
        pointerInputState.addPressed(0)
        pointerInputState.addPressed(1)
        inputStateTable.addInputState(sourceId, pointerInputState)
        inputStateTable.addInputState(sourceId2, pointerInputState)

        // Construct pointer move event
        val actionObjectOne = ActionObject(sourceId, POINTER, null, 0)
        actionObjectOne.subType = POINTER_MOVE
        actionObjectOne.pointer = TOUCH
        actionObjectOne.x = 10.0f
        actionObjectOne.y = 20.0f
        actionObjectOne.origin = Origin(Origin.VIEWPORT)

        // Construct another pointer move event
        val actionObjectTwo = ActionObject(sourceId2, POINTER, null, 0)
        actionObjectTwo.subType = POINTER_MOVE
        actionObjectTwo.pointer = TOUCH
        actionObjectTwo.x = 10.0f
        actionObjectTwo.y = 20.0f
        actionObjectTwo.origin = Origin(Origin.VIEWPORT)

        // Add two pointer move actions to verify that they can run on multiple threads separately
        tick.addAction(actionObjectOne)
        tick.addAction(actionObjectTwo)

        class ExtendedDummyW3CActionAdapter : DummyW3CActionAdapter() {
            @Suppress("UNUSED_PARAMETER", "unused")
            fun pointerMove(sourceId: String,
                            pointerType: PointerType,
                            currentX: Float?, currentY: Float?,
                            x: Float?, y: Float?,
                            buttons: Set<Int>,
                            globalKeyInputState: KeyInputState) {
                assertEquals(pointerType, TOUCH)
                assertFloatEquals(currentX!!, 5f)
                assertFloatEquals(currentY!!, 6f)
                assertFloatEquals(x!!, 10f)
                assertFloatEquals(y!!, 20f)
                assertTrue(buttons.contains(0))
                assertTrue(buttons.contains(1))
                assertTrue(globalKeyInputState.isShift)
            }
        }

        val dummyAdapter = ExtendedDummyW3CActionAdapter()

        val callables = tick.dispatchAll(dummyAdapter, inputStateTable, tick.calculateTickDuration())
        val executor = Executors.newFixedThreadPool(callables.size)
        val completionService = ExecutorCompletionService<BaseDispatchResult>(executor)
        for (callable in callables) {
            completionService.submit(callable)
        }

        var received = 0
        while (received < callables.size) {
            val resultFuture = completionService.take() //blocks if none available
            resultFuture.get()
            received++
        }
    }

    @Test
    @Throws(AppiumException::class)
    fun `should throw invalid cast if using same source ID but different type`() {
        val inputStateTable = InputStateTable()
        val tick = Tick()
        val sourceId = "something4"
        val pointerInputState = PointerInputState(TOUCH)
        inputStateTable.addInputState(sourceId, pointerInputState)

        // Depress the shift key
        val actionObjectTwo = ActionObject(sourceId, KEY, null, 0)
        actionObjectTwo.subType = KEY_DOWN

        // Construct pointer move event
        val actionObjectOne = ActionObject(sourceId, POINTER, null, 0)
        actionObjectOne.subType = POINTER_DOWN

        tick.addAction(actionObjectOne)
        tick.addAction(actionObjectTwo)

        try {
            tick.dispatchAll(DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration())
        } catch (e: InvalidArgumentException) {
            assertTrue(e.message!!.contains("Attempted to apply action of type"))
            return
        }

        fail("Should have called 'InvalidArgumentException'")
    }
}
