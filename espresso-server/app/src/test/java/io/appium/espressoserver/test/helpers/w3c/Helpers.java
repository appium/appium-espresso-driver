package io.appium.espressoserver.test.helpers.w3c;

import static org.junit.Assert.assertEquals;

public class Helpers {

    public static void assertFloatEquals(float floatOne, float floatTwo) {
        assertEquals(floatOne, floatTwo, Math.ulp(1.0));
    }
}
