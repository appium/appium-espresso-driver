package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter.PointerMoveEvent;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.models.Origin;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerDown;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerUp;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.performPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.POINTER;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PointerDispatchTest {

    private PointerInputState pointerInputSource;

    @Test
    public void shouldNoopPointerMoveIfNoButtons() throws ExecutionException, InterruptedException, AppiumException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState(TOUCH);
        pointerInputSource.setType(PointerType.TOUCH);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<BaseDispatchResult> callable = performPointerMove(
            dummyW3CActionAdapter, "any", pointerInputSource,
            100, 10, 20, 30, 40, System.currentTimeMillis(),
            new KeyInputState()
        );
        BaseDispatchResult dispatchResult = executorService.submit(callable).get();
        dispatchResult.perform();
        executorService.shutdown();
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size(), 1);
    }

    @Test
    public void shouldDoOneMoveIfDurationZero()
            throws ExecutionException, InterruptedException, AppiumException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState(TOUCH);
        pointerInputSource.setType(PointerType.TOUCH);
        pointerInputSource.setX(10);
        pointerInputSource.setY(20);
        pointerInputSource.addPressed(0);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<BaseDispatchResult> callable = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource,
                0, 10, 20, 30, 40, System.currentTimeMillis(),
                new KeyInputState()
        );
        BaseDispatchResult dispatchResult = executorService.submit(callable).get();
        dispatchResult.perform();
        executorService.shutdown();

        List<PointerMoveEvent> pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents();
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size(), 1);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).x, 30);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).y, 40);
    }

    @Test
    public void shouldMovePointerInIntervals() throws ExecutionException, InterruptedException, AppiumException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState(TOUCH);
        pointerInputSource.setType(PointerType.TOUCH);
        pointerInputSource.setX(10);
        pointerInputSource.setY(20);
        pointerInputSource.addPressed(0);

        Callable<BaseDispatchResult> callable = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource,
                1000, 10, 20, 30, 40, System.currentTimeMillis(),
                new KeyInputState()
        );

        BaseDispatchResult dispatchResult;
        do {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            dispatchResult = executorService.submit(callable).get();
            dispatchResult.perform();
            callable = dispatchResult.getNext();
            executorService.shutdown();
        } while (dispatchResult.hasNext());

        List<PointerMoveEvent> pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents();
        assertTrue(Math.abs(pointerMoveEvents.size() - 20) <= 2); // Should be 20 moves per the 1 second (give or take 1)
        assertEquals(pointerMoveEvents.get(0).currentX, 10);
        assertEquals(pointerMoveEvents.get(0).currentY, 20);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).x, 30);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).y, 40);

        long prevX = 10;
        long prevY = 10;
        long currX;
        long currY;
        for(PointerMoveEvent pointerMoveEvent:pointerMoveEvents) {
            currX = pointerMoveEvent.x;
            currY = pointerMoveEvent.y;
            assertTrue(currX > prevX);
            assertTrue(currY > prevY);
            prevX = currX;
            prevY = currY;
        }

        assertEquals(pointerInputSource.getX(), 30);
        assertEquals(pointerInputSource.getY(), 40);
    }

    @Test
    public void shouldRunMultiplePointerMoves() throws InterruptedException, ExecutionException, AppiumException{
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState(TOUCH);
        pointerInputSource.setType(PointerType.TOUCH);
        pointerInputSource.setX(10);
        pointerInputSource.setY(20);
        pointerInputSource.addPressed(0);

        PointerInputState pointerInputSourceTwo = new PointerInputState(TOUCH);
        pointerInputSourceTwo.setType(PointerType.TOUCH);
        pointerInputSourceTwo.setX(10);
        pointerInputSourceTwo.setY(20);
        pointerInputSourceTwo.addPressed(0);

        Callable<BaseDispatchResult> pointerMoveOne = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource,
                500, 10, 20, 30, 40, System.currentTimeMillis(),
                new KeyInputState()
        );

        Callable<BaseDispatchResult> pointerMoveTwo = performPointerMove(
                dummyW3CActionAdapter, "any2", pointerInputSourceTwo,
                500, 20, 30, 40, 50, System.currentTimeMillis(),
                new KeyInputState()
        );

        Executor executor = Executors.newCachedThreadPool();
        long completedPointerMoves = 0;

        do {
            CompletionService<BaseDispatchResult> completionService = new ExecutorCompletionService<>(executor);
            completionService.submit(pointerMoveOne);
            completionService.submit(pointerMoveTwo);

            Future<BaseDispatchResult> resultFuture = completionService.take(); //blocks if none available
            BaseDispatchResult dispatchResult = resultFuture.get();
            dispatchResult.perform();
            if (dispatchResult.hasNext()) {
                completionService.submit(dispatchResult.getNext());
            } else {
                completedPointerMoves++;
            }
        } while (completedPointerMoves < 2);
        List<PointerMoveEvent> pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents();

        boolean hasAny = false;
        boolean hasAny2 = false;
        for (PointerMoveEvent pointerMoveEvent:pointerMoveEvents) {
            if ("any".equals(pointerMoveEvent.sourceId)) {
                hasAny = true;
                assertTrue(pointerMoveEvent.x >= 10 && pointerMoveEvent.x <= 30);
                assertTrue(pointerMoveEvent.y >= 20 && pointerMoveEvent.y <= 40);
            } else if ("any2".equals(pointerMoveEvent.sourceId)) {
                hasAny2 = true;
                assertTrue(pointerMoveEvent.x >= 20 && pointerMoveEvent.x <= 40);
                assertTrue(pointerMoveEvent.y >= 30 && pointerMoveEvent.y <= 50);

            }
        }
        assertTrue(hasAny);
        assertTrue(hasAny2);
    }

    @Test
    public void shouldThrowBoundsExceptions() throws AppiumException {

        String V = VIEWPORT;
        String P = POINTER;
        String E = "element-6066-11e4-a52e-4f735466cecf";

        // Make a matrix of pointers that are out-of-bounds
        long[] badX =           new long[]  { -1,  0, 201, 200, 191, 190, 191, 190  };
        long[] badY =           new long[]  {  0, -1, 400, 401, 410, 411, 410, 411 };
        String[] badOrigin =    new String[]{  V,  V, V,   V,   E,   E,   P,   P };

        for (int i=0; i<badX.length; i++) {
            DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
            PointerInputState pointerInputState = new PointerInputState(TOUCH);

            pointerInputState.setX(10);
            pointerInputState.setY(10);
            ActionObject actionObject = new ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            );
            actionObject.setX(badX[i]);
            actionObject.setY(badY[i]);
            actionObject.setOrigin(new Origin(badOrigin[i]));

            try {
                dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                        pointerInputState, 10, 0, null);
            } catch (MoveTargetOutOfBoundsException me) {
                assertTrue(me.getMessage().contains("not in the viewport"));
                continue;
            }
            fail("Should have thrown 'MoveTargetOutOfBoundsException'");
        }
    }

    @Test
    public void shouldDispatchPointerMovesAndUpdateState() throws AppiumException, ExecutionException, InterruptedException {

        String V = VIEWPORT;
        String P = POINTER;
        String E = "element-6066-11e4-a52e-4f735466cecf";

        // Make a matrix of pointers and the expected state changes
        long[] xCoords =    new long[]  {  10, -5, 15, -5, 15  };
        long[] yCoords =    new long[]  {  15, -5, 25, -5, 25 };
        String[] origins =  new String[]{  V,  E,  E,  P,  P };
        long[] expectedX =  new long[]  {  10, 5,  25, 45, 65 };
        long[] expectedY =  new long[]  {  15, 5,  35, 65, 95 };

        for (int i=0; i<xCoords.length; i++) {
            DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
            PointerInputState pointerInputState = new PointerInputState(TOUCH);

            pointerInputState.setX(50);
            pointerInputState.setY(70);
            ActionObject actionObject = new ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            );
            actionObject.setX(xCoords[i]);
            actionObject.setY(yCoords[i]);
            actionObject.setOrigin(new Origin(origins[i]));

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Callable<BaseDispatchResult> callable = dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                pointerInputState, 10, 0, null);
            executorService.submit(callable).get();
            executorService.shutdown();

            assertEquals(pointerInputState.getX(), expectedX[i]);
            assertEquals(pointerInputState.getY(), expectedY[i]);
        }
    }

    @Test
    public void shouldAddButtonToDepressedOnDispatchDown() throws AppiumException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        InputStateTable inputStateTable = new InputStateTable();
        PointerInputState pointerInputState = new PointerInputState(TOUCH);

        ActionObject actionObject = new ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        );
        actionObject.setButton(1);

        assertFalse(pointerInputState.isPressed(1));
        assertTrue(inputStateTable.getCancelList().isEmpty());
        dispatchPointerDown(dummyW3CActionAdapter, actionObject, pointerInputState,
                inputStateTable,null);
        assertEquals(inputStateTable.getCancelList().size(), 1);
        ActionObject cancelObject = inputStateTable.getCancelList().get(0);
        assertEquals(cancelObject.getButton(), 1);
        assertEquals(cancelObject.getSubType(), POINTER_UP);
        assertTrue(pointerInputState.isPressed(1));
    }

    @Test
    public void shouldReturnDispatchDownRightAwayIfAlreadyPressed() throws AppiumException {
        class TempDummyAdapter extends DummyW3CActionAdapter {
            @Override
            public void pointerDown(int button, String sourceId, PointerType pointerType,
                                    Long x, Long y, Set<Integer> depressedButtons,
                                    KeyInputState globalKeyInputState) throws AppiumException {
                throw new AppiumException("Should not reach this point. Button already pressed.");
            }

        }
        W3CActionAdapter dummyW3CActionAdapter = new TempDummyAdapter();
        PointerInputState pointerInputState = new PointerInputState(TOUCH);
        pointerInputState.addPressed(1);
        ActionObject actionObject = new ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        );
        actionObject.setButton(1);

        assertTrue(pointerInputState.isPressed(1));
        dispatchPointerDown(dummyW3CActionAdapter, actionObject, pointerInputState,
                new InputStateTable(), null);

        assertTrue(pointerInputState.isPressed(1));
    }

    @Test
    public void shouldRemoveButtonFromDepressedOnDispatchUp() throws AppiumException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        PointerInputState pointerInputState = new PointerInputState(TOUCH);
        pointerInputState.addPressed(1);

        ActionObject actionObject = new ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        );
        actionObject.setButton(1);

        assertTrue(pointerInputState.isPressed(1));
        dispatchPointerUp(dummyW3CActionAdapter, actionObject, pointerInputState,
                new InputStateTable(), null);

        assertFalse(pointerInputState.isPressed(1));
    }

    @Test
    public void shouldReturnDispatchUpRightAwayIfNotCurrentlyPressed() throws AppiumException {
        class TempDummyAdapter extends DummyW3CActionAdapter {
            @Override
            public void pointerUp(int button, String sourceId, PointerType pointerType,
                                  Long x, Long y, Set<Integer> depressedButtons,
                                  KeyInputState globalKeyInputState) throws AppiumException {
                throw new AppiumException("Should not reach this point. Button already pressed.");
            }

        }
        W3CActionAdapter dummyW3CActionAdapter = new TempDummyAdapter();
        PointerInputState pointerInputState = new PointerInputState(TOUCH);
        ActionObject actionObject = new ActionObject(
                "id", InputSourceType.POINTER, POINTER_MOVE, 0
        );
        actionObject.setButton(1);

        assertFalse(pointerInputState.isPressed(1));
        dispatchPointerUp(dummyW3CActionAdapter, actionObject, pointerInputState,
                new InputStateTable(), null);

        assertFalse(pointerInputState.isPressed(1));
    }
}
