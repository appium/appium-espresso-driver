package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.MoveTargetOutOfBoundsException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter.PointerMoveEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.dispatchPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.performPointerMove;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.*;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.POINTER;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PointerDispatchTest {

    private PointerInputState pointerInputSource;

    @Before
    public void before() {
    }


    @Test
    public void shouldNoopPointerMoveIfNoButtons() throws ExecutionException, InterruptedException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState();
        pointerInputSource.setType(PointerType.TOUCH);

        Future<Void> future = performPointerMove(
            dummyW3CActionAdapter, "any", pointerInputSource,
            100, 10, 20, 30, 40, System.currentTimeMillis(),
            new KeyInputState()
        );
        future.get();
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size(), 1);
    }

    @Test
    public void shouldDoOneMoveIfDurationZero() throws ExecutionException, InterruptedException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState();
        pointerInputSource.setType(PointerType.TOUCH);
        pointerInputSource.setX(10);
        pointerInputSource.setY(20);
        pointerInputSource.addPressed(0);

        Future<Void> future = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource,
                0, 10, 20, 30, 40, System.currentTimeMillis(),
                new KeyInputState()
        );
        future.get();
        List<PointerMoveEvent> pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents();
        assertEquals(dummyW3CActionAdapter.getPointerMoveEvents().size(), 1);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).x, 30);
        assertEquals(pointerMoveEvents.get(pointerMoveEvents.size() - 1).y, 40);
    }

    @Test
    public void shouldMovePointerInIntervals() throws ExecutionException, InterruptedException {
        DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
        pointerInputSource = new PointerInputState();
        pointerInputSource.setType(PointerType.TOUCH);
        pointerInputSource.setX(10);
        pointerInputSource.setY(20);
        pointerInputSource.addPressed(0);

        Future<Void> future = performPointerMove(
                dummyW3CActionAdapter, "any", pointerInputSource,
                1000, 10, 20, 30, 40, System.currentTimeMillis(),
                new KeyInputState()
        );
        future.get();
        List<PointerMoveEvent> pointerMoveEvents = dummyW3CActionAdapter.getPointerMoveEvents();
        assertTrue(Math.abs(pointerMoveEvents.size() - 15) <= 1); // Should be 15 moves per the 1 second (give or take 1)
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
            prevY = currX;
        }

        assertEquals(pointerInputSource.getX(), 30);
        assertEquals(pointerInputSource.getY(), 40);
    }

    @Test
    public void shouldThrowBoundsExceptions() throws AppiumException {

        String V = VIEWPORT;
        String P = POINTER;
        String E = "element";

        // Make a matrix of pointers that are out-of-bounds
        long[] badX =           new long[]  { -1,  0, 201, 200, 191, 190, 191, 190  };
        long[] badY =           new long[]  {  0, -1, 400, 401, 410, 411, 410, 411 };
        String[] badOrigin =    new String[]{  V,  V, V,   V,   E,   E,   P,   P };

        for (int i=0; i<badX.length; i++) {
            DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
            PointerInputState pointerInputState = new PointerInputState();

            pointerInputState.setX(10);
            pointerInputState.setY(10);
            ActionObject actionObject = new ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            );
            actionObject.setX(badX[i]);
            actionObject.setY(badY[i]);
            actionObject.setOrigin(badOrigin[i]);

            try {
                dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                        pointerInputState, 10, 0, null);
            } catch (MoveTargetOutOfBoundsException me) {
                assertTrue(me.getMessage().contains("not in the viewport"));
                continue;
            }
            assertTrue(false);
        }
    }

    @Test
    public void shouldDispatchPointerMovesAndUpdateState() throws AppiumException, ExecutionException, InterruptedException {

        String V = VIEWPORT;
        String P = POINTER;
        String E = "element";

        // Make a matrix of pointers and the expected state changes
        long[] xCoords =    new long[]  {  10, -5, 15, -5, 15  };
        long[] yCoords =    new long[]  {  15, -5, 25, -5, 25 };
        String[] origins =  new String[]{  V,  E,  E,  P,  P };
        long[] expectedX =  new long[]  {  10, 5,  25, 45, 65 };
        long[] expectedY =  new long[]  {  15, 5,  35, 65, 95 };

        for (int i=0; i<xCoords.length; i++) {
            DummyW3CActionAdapter dummyW3CActionAdapter = new DummyW3CActionAdapter();
            PointerInputState pointerInputState = new PointerInputState();

            pointerInputState.setX(50);
            pointerInputState.setY(70);
            ActionObject actionObject = new ActionObject(
                    "id", InputSourceType.POINTER, POINTER_MOVE, 0
            );
            actionObject.setX(xCoords[i]);
            actionObject.setY(yCoords[i]);
            actionObject.setOrigin(origins[i]);

            dispatchPointerMove(dummyW3CActionAdapter, "any", actionObject,
                pointerInputState, 10, 0, null).get();

            assertEquals(pointerInputState.getX(), expectedX[i]);
            assertEquals(pointerInputState.getY(), expectedY[i]);
        }
    }
}
