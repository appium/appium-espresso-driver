package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter.PointerMoveEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.PointerDispatch.performPointerMove;
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

        int prevX = 10;
        int prevY = 10;
        int currX;
        int currY;
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
}
