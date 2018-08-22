package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.DummyW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.BaseDispatchResult;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.models.Origin;
import io.appium.espressoserver.lib.helpers.w3c.models.Tick;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.NONE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.POINTER;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.VIEWPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TickTest {

    @Test
    public void shouldCalculateMaxDurationZeroIfNoDurations() throws ExecutionException, InterruptedException {
        Tick tick = new Tick();
        tick.addAction(new ActionObject());
        assertEquals(tick.calculateTickDuration(), 0);
    }

    @Test
    public void shouldCalculateMaxDuration() {

        Long[] valueOne =    new Long[] { 10L, 30L, 0L };
        Long[] valueTwo =    new Long[] { 20L, 10L, 1L };
        long[] expectedMax = new long[] { 20L, 30L, 1L };

        for (int i=0; i<valueOne.length; i++) {
            Tick tick = new Tick();
            ActionObject actionObjectOne = new ActionObject();
            actionObjectOne.setType(NONE);
            actionObjectOne.setSubType(PAUSE);
            actionObjectOne.setDuration(valueOne[i]);

            ActionObject actionObjectTwo = new ActionObject();
            actionObjectTwo.setType(POINTER);
            actionObjectTwo.setSubType(POINTER_MOVE);
            actionObjectTwo.setDuration(valueTwo[i]);

            ActionObject actionObjectThree = new ActionObject();
            actionObjectTwo.setType(POINTER);
            actionObjectTwo.setSubType(POINTER_MOVE);

            tick.addAction(actionObjectOne);
            tick.addAction(actionObjectTwo);
            tick.addAction(actionObjectThree);

            assertEquals(tick.calculateTickDuration(), expectedMax[i]);
        }
    }

    @Test
    public void shouldAddKeyInputStateWhenRunningActions() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        Tick tick = new Tick();
        String sourceId = "something1";
        ActionObject actionObject = new ActionObject(sourceId, KEY, null, 0);
        actionObject.setSubType(KEY_DOWN);
        actionObject.setValue("F");
        tick.addAction(actionObject);
        assertFalse(inputStateTable.hasInputState(sourceId));
        tick.dispatchAll(new DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration());
        assertTrue(inputStateTable.hasInputState(sourceId));
        assertTrue(inputStateTable.getInputState(sourceId).getClass() == KeyInputState.class);
    }

    @Test
    public void shouldAddPointerInputStateWhenRunningActions() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        Tick tick = new Tick();
        String sourceId = "something2";
        ActionObject actionObject = new ActionObject(sourceId, POINTER, null, 0);
        actionObject.setSubType(POINTER_DOWN);
        actionObject.setButton(0);
        tick.addAction(actionObject);
        assertFalse(inputStateTable.hasInputState(sourceId));
        tick.dispatchAll(new DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration());
        assertTrue(inputStateTable.hasInputState(sourceId));
        assertTrue(inputStateTable.getInputState(sourceId).getClass() == PointerInputState.class);
    }

    @Test
    public void shouldNotAddNoneToState() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        Tick tick = new Tick();
        String sourceId = "something3";
        ActionObject actionObject = new ActionObject(sourceId, NONE, null, 0);
        tick.addAction(actionObject);
        assertFalse(inputStateTable.hasInputState(sourceId));
        tick.dispatchAll(new DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration());
        assertFalse(inputStateTable.hasInputState(sourceId));
    }

    @Test
    public void shouldDispatchKeyEvents() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        Tick tick = new Tick();
        String sourceId = "something4";
        KeyInputState keyInputState = new KeyInputState();
        keyInputState.addPressed("g");
        inputStateTable.addInputState(sourceId, keyInputState);
        ActionObject actionObjectOne = new ActionObject(sourceId, KEY, null, 0);
        actionObjectOne.setSubType(KEY_DOWN);
        actionObjectOne.setValue("e");

        ActionObject actionObjectTwo = new ActionObject(sourceId, KEY, null, 0);
        actionObjectTwo.setSubType(KEY_DOWN);
        actionObjectTwo.setValue("f");

        ActionObject actionObjectThree = new ActionObject(sourceId, KEY, null, 0);
        actionObjectThree.setSubType(KEY_UP);
        actionObjectThree.setValue("g");

        ActionObject actionObjectFour = new ActionObject(sourceId, KEY, null, 0);
        actionObjectFour.setSubType(KEY_DOWN);
        actionObjectFour.setValue("\uE008");

        tick.addAction(actionObjectOne);
        tick.addAction(actionObjectTwo);
        tick.addAction(actionObjectThree);
        tick.addAction(actionObjectFour);

        keyInputState = (KeyInputState) inputStateTable.getInputState(sourceId);
        assertFalse(keyInputState.isPressed("e"));
        assertFalse(keyInputState.isPressed("f"));
        assertTrue(keyInputState.isPressed("g"));
        assertFalse(keyInputState.isShift());

        tick.dispatchAll(new DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration());
        assertTrue(keyInputState.isPressed("e"));
        assertTrue(keyInputState.isPressed("f"));
        assertFalse(keyInputState.isPressed("g"));
        assertTrue(keyInputState.isShift());
    }

    @Test
    public void shouldDispatchPointerMoveEvents() throws AppiumException, InterruptedException, ExecutionException {
        final InputStateTable inputStateTable = new InputStateTable();
        KeyInputState keyInputState = new KeyInputState();
        keyInputState.setShift(true);
        inputStateTable.addInputState("keyInputs", keyInputState);
        final Tick tick = new Tick();

        final String sourceId = "something4";
        final String sourceId2 = "something5";
        PointerInputState pointerInputState = new PointerInputState(TOUCH);
        pointerInputState.setType(TOUCH);
        pointerInputState.setX(5L);
        pointerInputState.setY(6L);
        pointerInputState.addPressed(0);
        pointerInputState.addPressed(1);
        inputStateTable.addInputState(sourceId, pointerInputState);
        inputStateTable.addInputState(sourceId2, pointerInputState);

        // Construct pointer move event
        ActionObject actionObjectOne = new ActionObject(sourceId, POINTER, null, 0);
        actionObjectOne.setSubType(POINTER_MOVE);
        actionObjectOne.setPointer(TOUCH);
        actionObjectOne.setX(10L);
        actionObjectOne.setY(20L);
        actionObjectOne.setOrigin(new Origin(VIEWPORT));

        // Construct another pointer move event
        ActionObject actionObjectTwo = new ActionObject(sourceId2, POINTER, null, 0);
        actionObjectTwo.setSubType(POINTER_MOVE);
        actionObjectTwo.setPointer(TOUCH);
        actionObjectTwo.setX(10L);
        actionObjectTwo.setY(20L);
        actionObjectTwo.setOrigin(new Origin(VIEWPORT));

        // Add two pointer move actions to verify that they can run on multiple threads separately
        tick.addAction(actionObjectOne);
        tick.addAction(actionObjectTwo);

        class ExtendedDummyW3CActionAdapter extends DummyW3CActionAdapter {
            @Override
            public void pointerMove(String sourceIdCalled,
                                    PointerType pointerType,
                                    long currentX, long currentY,
                                    long x, long y,
                                    Set<Integer> buttons,
                                    KeyInputState globalKeyInputState) throws AppiumException {
                assertEquals(pointerType, TOUCH);
                assertEquals(currentX, 5L);
                assertEquals(currentY, 6L);
                assertEquals(x, 10L);
                assertEquals(y, 20L);
                assertTrue(buttons.contains(0));
                assertTrue(buttons.contains(1));
                assertTrue(globalKeyInputState.isShift());
            }
        };

        final W3CActionAdapter dummyAdapter = new ExtendedDummyW3CActionAdapter();

        List<Callable<BaseDispatchResult>> callables = tick.dispatchAll(dummyAdapter, inputStateTable, tick.calculateTickDuration());
        Executor executor = Executors.newFixedThreadPool(callables.size());
        CompletionService<BaseDispatchResult> completionService = new ExecutorCompletionService<>(executor);
        for(Callable<BaseDispatchResult> callable:callables) {
            completionService.submit(callable);
        }

        int received = 0;
        while (received < callables.size()) {
            Future<BaseDispatchResult> resultFuture = completionService.take(); //blocks if none available
            resultFuture.get();
            received ++;
        }
    }

    @Test
    public void shouldInvalidCastForSameSourceIdDifferentType() throws AppiumException {
        InputStateTable inputStateTable = new InputStateTable();
        Tick tick = new Tick();
        final String sourceId = "something4";
        PointerInputState pointerInputState = new PointerInputState(TOUCH);
        inputStateTable.addInputState(sourceId, pointerInputState);

        // Depress the shift key
        ActionObject actionObjectTwo = new ActionObject(sourceId, KEY, null, 0);
        actionObjectTwo.setSubType(KEY_DOWN);

        // Construct pointer move event
        ActionObject actionObjectOne = new ActionObject(sourceId, POINTER, null, 0);
        actionObjectOne.setSubType(POINTER_DOWN);

        tick.addAction(actionObjectOne);
        tick.addAction(actionObjectTwo);

        try {
            tick.dispatchAll(new DummyW3CActionAdapter(), inputStateTable, tick.calculateTickDuration());
        } catch (InvalidArgumentException e) {
            assertTrue(e.getMessage().contains("Attempted to apply action of type"));
            return;
        }

        fail("Should have called 'InvalidArgumentException'");
    }
}
