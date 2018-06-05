package io.appium.espressoserver.test.helpers.w3c;


import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static org.junit.Assert.assertTrue;


public class KeyInputStateTest {

    // Start tests for getAggregateKeyInputState

    KeyInputState keyInputStateOne;
    KeyInputState keyInputStateTwo;

    @Before
    public void before() {
        keyInputStateOne = new KeyInputState();
        keyInputStateTwo = new KeyInputState();
    }

    @Test
    public void shouldSetAltToTrueIfJustOneIsTrue() {
        keyInputStateOne.setAlt(true);
        keyInputStateTwo.setAlt(false);
        KeyInputState[] keyInputStates = new KeyInputState[]{keyInputStateOne, keyInputStateTwo};
        KeyInputState aggregateKeyInputState = KeyInputState.getAggregateKeyInputState(Arrays.asList(keyInputStates));
        assertTrue(aggregateKeyInputState.isAlt());
    }

    @Test
    public void shouldSetAltToFalseIfAllAreFalse() {
        keyInputStateOne.setAlt(false);
        keyInputStateTwo.setAlt(false);
        KeyInputState[] keyInputStates = new KeyInputState[]{keyInputStateOne, keyInputStateTwo};
        KeyInputState aggregateKeyInputState = KeyInputState.getAggregateKeyInputState(Arrays.asList(keyInputStates));
        assertTrue(!aggregateKeyInputState.isAlt());
    }

    @Test
    public void shouldTestSeveralKeyStates() {
        keyInputStateOne.setAlt(true);
        keyInputStateOne.setCtrl(true);
        keyInputStateOne.setShift(false);
        keyInputStateOne.setMeta(false);

        keyInputStateTwo.setAlt(false);
        keyInputStateTwo.setCtrl(true);
        keyInputStateTwo.setShift(false);
        keyInputStateTwo.setMeta(true);

        KeyInputState[] keyInputStates = new KeyInputState[]{keyInputStateOne, keyInputStateTwo};
        KeyInputState aggregateKeyInputState = KeyInputState.getAggregateKeyInputState(Arrays.asList(keyInputStates));
        assertTrue(aggregateKeyInputState.isAlt());
        assertTrue(aggregateKeyInputState.isCtrl());
        assertTrue(!aggregateKeyInputState.isShift());
        assertTrue(aggregateKeyInputState.isMeta());
    }

    @Test
    public void shouldCombinePressed() {
        keyInputStateOne.addPressed("a");
        keyInputStateOne.addPressed("b");
        keyInputStateOne.addPressed("c");
        keyInputStateOne.addPressed("d");
        keyInputStateTwo.addPressed("d");
        keyInputStateTwo.addPressed("e");
        keyInputStateTwo.addPressed("f");

        KeyInputState[] keyInputStates = new KeyInputState[]{keyInputStateOne, keyInputStateTwo};
        KeyInputState aggregateKeyInputState = KeyInputState.getAggregateKeyInputState(Arrays.asList(keyInputStates));
        assertTrue(aggregateKeyInputState.isPressed("a"));
        assertTrue(aggregateKeyInputState.isPressed("b"));
        assertTrue(aggregateKeyInputState.isPressed("c"));
        assertTrue(aggregateKeyInputState.isPressed("d"));
        assertTrue(aggregateKeyInputState.isPressed("e"));
        assertTrue(aggregateKeyInputState.isPressed("f"));
        assertTrue(!aggregateKeyInputState.isPressed("g"));
    }
}