package io.appium.espressoserver.test.helpers.w3c

import org.junit.Assert

fun assertFloatEquals(floatOne: Float, floatTwo: Float) {
    Assert.assertEquals(floatOne.toDouble(), floatTwo.toDouble(), Math.ulp(1.0))
}