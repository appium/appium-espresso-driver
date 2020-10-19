package io.appium.espressoserver.test.helpers.w3c

import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType
import io.appium.espressoserver.lib.helpers.w3c.models.Origin
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState

import io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerDown
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerMove
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerUp
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.performPointerMove
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_UP
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import kotlin.math.abs

class PointerDispatchTest {

    private var pointerInputSource: PointerInputState? = null

    @Test
    @Throws(ExecutionException::class, InterruptedException::class, AppiumException::class)
    fun `should no-op pointer move if no buttons`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        pointerInputSource = PointerInputState(TOUCH)
        pointerInputSource!!.type = TOUCH

        val executorService = Executors.newSingleThreadExecutor()
        val callable = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource!!,
                100f, 10f, 20f, 30f, 40f, System.currentTimeMillis(),
                KeyInputState()
        )
        val dispatchResult = executorService.submit(callable).get()
        dispatchResult.perform()
        executorService.shutdown()
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size.toLong(), 1)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class, AppiumException::class)
    fun `should do one move if duration zero`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        pointerInputSource = PointerInputState(TOUCH)
        pointerInputSource!!.type = TOUCH
        pointerInputSource!!.x = 10f
        pointerInputSource!!.y = 20f
        pointerInputSource!!.addPressed(0)

        val executorService = Executors.newSingleThreadExecutor()
        val callable = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource!!,
                0f, 10f, 20f, 30f, 40f, System.currentTimeMillis(),
                KeyInputState()
        )
        val dispatchResult = executorService.submit(callable).get()
        dispatchResult.perform()
        executorService.shutdown()

        val pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents()
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size.toLong(), 1)
        assertFloatEquals(pointerMoveEvents[pointerMoveEvents.size - 1].x, 30f)
        assertFloatEquals(pointerMoveEvents[pointerMoveEvents.size - 1].y, 40f)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class, AppiumException::class)
    fun `should move pointer in intervals`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        pointerInputSource = PointerInputState(TOUCH)
        pointerInputSource!!.type = TOUCH
        pointerInputSource!!.x = 10f
        pointerInputSource!!.y = 20f
        pointerInputSource!!.addPressed(0)

        var callable: Callable<BaseDispatchResult>? = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource!!,
                1000f, 10f, 20f, 30f, 40f, System.currentTimeMillis(),
                KeyInputState()
        )

        var dispatchResult: BaseDispatchResult
        do {
            val executorService = Executors.newSingleThreadExecutor()
            dispatchResult = executorService.submit(callable).get()
            dispatchResult.perform()
            callable = dispatchResult.next
            executorService.shutdown()
        } while (dispatchResult.hasNext())

        val pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents()
        assertTrue(abs(pointerMoveEvents.size - 20) <= 2) // Should be 20 moves per the 1 second (give or take 1)
        assertFloatEquals(pointerMoveEvents[0].currentX, 10f)
        assertFloatEquals(pointerMoveEvents[0].currentY, 20f)
        assertFloatEquals(pointerMoveEvents[pointerMoveEvents.size - 1].x, 30f)
        assertFloatEquals(pointerMoveEvents[pointerMoveEvents.size - 1].y, 40f)

        var prevX = 10f
        var prevY = 10f
        var currX: Float
        var currY: Float
        for (pointerMoveEvent in pointerMoveEvents) {
            currX = pointerMoveEvent.x
            currY = pointerMoveEvent.y
            assertTrue(currX > prevX)
            assertTrue(currY > prevY)
            prevX = currX
            prevY = currY
        }

        assertFloatEquals(pointerInputSource!!.x, 30f)
        assertFloatEquals(pointerInputSource!!.y, 40f)
    }

    @Test
    @Throws(InterruptedException::class, ExecutionException::class, AppiumException::class)
    fun `should run multiple pointer moves`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        pointerInputSource = PointerInputState(TOUCH)
        pointerInputSource!!.type = TOUCH
        pointerInputSource!!.x = 10f
        pointerInputSource!!.y = 20f
        pointerInputSource!!.addPressed(0)

        val pointerInputSourceTwo = PointerInputState(TOUCH)
        pointerInputSourceTwo.type = TOUCH
        pointerInputSourceTwo.x = 10f
        pointerInputSourceTwo.y = 20f
        pointerInputSourceTwo.addPressed(0)

        val pointerMoveOne = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource!!,
                500f, 10f, 20f, 30f, 40f, System.currentTimeMillis(),
                KeyInputState()
        )

        val pointerMoveTwo = performPointerMove(
                dummyW3CActionAdapter, "any2", pointerInputSourceTwo,
                500f, 20f, 30f, 40f, 50f, System.currentTimeMillis(),
                KeyInputState()
        )

        val executor = Executors.newCachedThreadPool()
        var completedPointerMoves: Long = 0

        do {
            val completionService = ExecutorCompletionService<BaseDispatchResult>(executor)
            completionService.submit(pointerMoveOne)
            completionService.submit(pointerMoveTwo)

            val resultFuture = completionService.take() //blocks if none available
            val dispatchResult = resultFuture.get()
            dispatchResult.perform()
            if (dispatchResult.hasNext()) {
                completionService.submit(dispatchResult.next)
            } else {
                completedPointerMoves++
            }
        } while (completedPointerMoves < 2)
        val pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents()

        var hasAny = false
        var hasAny2 = false
        for (pointerMoveEvent in pointerMoveEvents) {
            if ("any" == pointerMoveEvent.sourceId) {
                hasAny = true
                assertTrue(pointerMoveEvent.x in 10.0..30.0)
                assertTrue(pointerMoveEvent.y in 20.0..40.0)
            } else if ("any2" == pointerMoveEvent.sourceId) {
                hasAny2 = true
                assertTrue(pointerMoveEvent.x in 20.0..40.0)
                assertTrue(pointerMoveEvent.y in 30.0..50.0)
            }
        }
        assertTrue(hasAny)
        assertTrue(hasAny2)
    }

    @Test
    @Throws(AppiumException::class)
    fun shouldThrowBoundsExceptions() {

        val V = Origin.VIEWPORT
        val P = Origin.POINTER
        val E = Origin.ELEMENT

        // Make a matrix of pointers that are out-of-bounds
        val badX = longArrayOf(-1, 0, 201, 200, 191, 190, 191, 190)
        val badY = longArrayOf(0, -1, 400, 401, 410, 411, 410, 411)
        val badOrigin = arrayOf(V, V, V, V, E, E, P, P)

        for (i in badX.indices) {
            val dummyW3CActionAdapter = DummyW3CActionAdapter()
            val pointerInputState = PointerInputState(TOUCH)

            pointerInputState.x = 10f
            pointerInputState.y = 10f
            val actionObject = ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            )
            actionObject.x = badX[i].toFloat()
            actionObject.y = badY[i].toFloat()
            actionObject.origin = Origin(badOrigin[i], "fake")

            try {
                dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                        pointerInputState, 10f, 0, null)
            } catch (me: MoveTargetOutOfBoundsException) {
                assertTrue(me.message!!.contains("not in the viewport"))
                continue
            }

            fail("Should have thrown 'MoveTargetOutOfBoundsException'")
        }
    }

    @Test
    @Throws(AppiumException::class, ExecutionException::class, InterruptedException::class)
    fun `should dispatch pointer moves and update state`() {

        val V = Origin.VIEWPORT
        val P = Origin.POINTER
        val E = Origin.ELEMENT

        // Make a matrix of pointers and the expected state changes
        val xCoords = longArrayOf(10, -5, 15, -5, 15)
        val yCoords = longArrayOf(15, -5, 25, -5, 25)
        val origins = arrayOf(V, E, E, P, P)
        val expectedX = longArrayOf(10, 5, 25, 45, 65)
        val expectedY = longArrayOf(15, 5, 35, 65, 95)

        for (i in xCoords.indices) {
            val dummyW3CActionAdapter = DummyW3CActionAdapter()
            val pointerInputState = PointerInputState(TOUCH)

            pointerInputState.x = 50f
            pointerInputState.y = 70f
            val actionObject = ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            )
            actionObject.x = xCoords[i].toFloat()
            actionObject.y = yCoords[i].toFloat()
            actionObject.origin = Origin(origins[i], "fake")

            val executorService = Executors.newSingleThreadExecutor()
            val callable = dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                    pointerInputState, 10f, 0, null)
            executorService.submit(callable).get()
            executorService.shutdown()

            assertFloatEquals(pointerInputState.x, expectedX[i].toFloat())
            assertFloatEquals(pointerInputState.y, expectedY[i].toFloat())
        }
    }

    @Test
    @Throws(AppiumException::class)
    fun `should add button to depressed on dispatched down event`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        val inputStateTable = InputStateTable()
        val pointerInputState = PointerInputState(TOUCH)

        val actionObject = ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        )
        actionObject.button = 1

        assertFalse(pointerInputState.isPressed(1))
        assertTrue(inputStateTable.cancelList.isEmpty())
        dispatchPointerDown(dummyW3CActionAdapter, actionObject, pointerInputState,
                inputStateTable, KeyInputState())
        assertEquals(inputStateTable.cancelList.size.toLong(), 1)
        val cancelObject = inputStateTable.cancelList[0]
        assertEquals(cancelObject.button.toLong(), 1)
        assertEquals(cancelObject.subType, POINTER_UP)
        assertTrue(pointerInputState.isPressed(1))
    }

    @Test
    @Throws(AppiumException::class)
    fun `should call dispatch down immediately if button already pressed`() {
        class TempDummyAdapter : DummyW3CActionAdapter() {
            @Throws(AppiumException::class)
            override fun pointerDown(button: Int, sourceId: String, pointerType: PointerType?,
                            x: Float, y: Float, depressedButtons: Set<Int>,
                            globalKeyInputState: KeyInputState?) {
                throw AppiumException("Should not reach this point. Button already pressed.")
            }

        }

        val dummyW3CActionAdapter = TempDummyAdapter()
        val pointerInputState = PointerInputState(TOUCH)
        pointerInputState.addPressed(1)
        val actionObject = ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        )
        actionObject.button = 1

        assertTrue(pointerInputState.isPressed(1))
        dispatchPointerDown(dummyW3CActionAdapter, actionObject, pointerInputState,
                InputStateTable(), null)

        assertTrue(pointerInputState.isPressed(1))
    }

    @Test
    @Throws(AppiumException::class)
    fun `should remove button from depressed button set on dispatch up`() {
        val dummyW3CActionAdapter = DummyW3CActionAdapter()
        val pointerInputState = PointerInputState(TOUCH)
        pointerInputState.addPressed(1)

        val actionObject = ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        )
        actionObject.button = 1

        assertTrue(pointerInputState.isPressed(1))
        dispatchPointerUp(dummyW3CActionAdapter, actionObject, pointerInputState,
                InputStateTable(), KeyInputState())

        assertFalse(pointerInputState.isPressed(1))
    }

    @Test
    @Throws(AppiumException::class)
    fun `should return dispatch up immediately if button is not pressed`() {
        class TempDummyAdapter : DummyW3CActionAdapter() {
            @Throws(AppiumException::class)
            @Suppress("UNUSED_PARAMETER", "unused")
            fun pointerUp(button: Int, sourceId: String, pointerType: PointerType,
                          x: Float?, y: Float?, depressedButtons: Set<Int>,
                          globalKeyInputState: KeyInputState?) {
                throw AppiumException("Should not reach this point. Button already pressed.")
            }

        }

        val dummyW3CActionAdapter = TempDummyAdapter()
        val pointerInputState = PointerInputState(TOUCH)
        val actionObject = ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        )
        actionObject.button = 1

        assertFalse(pointerInputState.isPressed(1))
        dispatchPointerUp(dummyW3CActionAdapter, actionObject, pointerInputState,
                InputStateTable(), null)

        assertFalse(pointerInputState.isPressed(1))
    }
}
