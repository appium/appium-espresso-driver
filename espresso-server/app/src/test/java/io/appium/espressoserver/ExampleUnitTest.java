package io.appium.espressoserver;

import org.junit.Test;

import io.appium.espressoserver.lib.model.AppiumParams;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void additionIsCorrect() throws Exception {
        AppiumParams appiumParams = new AppiumParams();
        assertNull(appiumParams.getElementId());
    }
}