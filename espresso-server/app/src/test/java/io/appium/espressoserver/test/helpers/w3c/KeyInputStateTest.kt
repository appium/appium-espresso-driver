package io.appium.espressoserver.test.helpers.w3c

import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState
import io.appium.espressoserver.lib.helpers.w3c.state.getGlobalKeyState
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class KeyInputStateTest {
    private var keyInputStateOne: KeyInputState? = null
    private var keyInputStateTwo: KeyInputState? = null

    @Before
    fun before() {
        keyInputStateOne = KeyInputState()
        keyInputStateTwo = KeyInputState()
    }

    @Test
    fun shouldSetAltToTrueIfJustOneIsTrue() {
        keyInputStateOne!!.isAlt = true
        keyInputStateTwo!!.isAlt = false
        val keyInputStates = arrayOf(keyInputStateOne, keyInputStateTwo)
        val aggregateKeyInputState = getGlobalKeyState(listOfNotNull(*keyInputStates))
        Assert.assertTrue(aggregateKeyInputState.isAlt)
    }

    @Test
    fun shouldSetAltToFalseIfAllAreFalse() {
        keyInputStateOne!!.isAlt = false
        keyInputStateTwo!!.isAlt = false
        val keyInputStates = arrayOf(keyInputStateOne, keyInputStateTwo)
        val aggregateKeyInputState = getGlobalKeyState(listOfNotNull(*keyInputStates))
        Assert.assertFalse(aggregateKeyInputState.isAlt)
    }

    @Test
    fun shouldTestSeveralKeyStates() {
        keyInputStateOne!!.isAlt = true
        keyInputStateOne!!.isCtrl = true
        keyInputStateOne!!.isShift = false
        keyInputStateOne!!.isMeta = false
        keyInputStateTwo!!.isAlt = false
        keyInputStateTwo!!.isCtrl = true
        keyInputStateTwo!!.isShift = false
        keyInputStateTwo!!.isMeta = true
        val keyInputStates = arrayOf(keyInputStateOne, keyInputStateTwo)
        val aggregateKeyInputState = getGlobalKeyState(listOfNotNull(*keyInputStates))
        Assert.assertTrue(aggregateKeyInputState.isAlt)
        Assert.assertTrue(aggregateKeyInputState.isCtrl)
        Assert.assertFalse(aggregateKeyInputState.isShift)
        Assert.assertTrue(aggregateKeyInputState.isMeta)
    }

    @Test
    fun shouldCombinePressed() {
        keyInputStateOne!!.addPressed("a")
        keyInputStateOne!!.addPressed("b")
        keyInputStateOne!!.addPressed("c")
        keyInputStateOne!!.addPressed("d")
        keyInputStateTwo!!.addPressed("d")
        keyInputStateTwo!!.addPressed("e")
        keyInputStateTwo!!.addPressed("f")
        val keyInputStates = arrayOf(keyInputStateOne, keyInputStateTwo)
        val aggregateKeyInputState = getGlobalKeyState(listOfNotNull(*keyInputStates))
        Assert.assertTrue(aggregateKeyInputState.isPressed("a"))
        Assert.assertTrue(aggregateKeyInputState.isPressed("b"))
        Assert.assertTrue(aggregateKeyInputState.isPressed("c"))
        Assert.assertTrue(aggregateKeyInputState.isPressed("d"))
        Assert.assertTrue(aggregateKeyInputState.isPressed("e"))
        Assert.assertTrue(aggregateKeyInputState.isPressed("f"))
        Assert.assertFalse(aggregateKeyInputState.isPressed("g"))
    }
}