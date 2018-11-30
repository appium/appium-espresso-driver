package io.appium.espressoserver.test.helpers.w3c.adapter;

import android.graphics.Point;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;

import static android.view.MotionEvent.TOOL_TYPE_FINGER;
import static android.view.MotionEvent.TOOL_TYPE_MOUSE;
import static android.view.MotionEvent.TOOL_TYPE_STYLUS;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.getToolType;
import static io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.Helpers.toCoordinates;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class HelpersTest {

    @Test
    public void getToolTypeDefaultIsFinger() throws AppiumException {
        assertEquals(getToolType(null), TOOL_TYPE_FINGER);
    }

    @Test
    public void getToolTypeTest() throws AppiumException {
        assertEquals(getToolType(PointerType.MOUSE), TOOL_TYPE_MOUSE);
        assertEquals(getToolType(PointerType.PEN), TOOL_TYPE_STYLUS);
        assertEquals(getToolType(PointerType.TOUCH), TOOL_TYPE_FINGER);
    }

    @Test
    public void toCoordinatesTest() {
        assertEquals(toCoordinates(123.0F, 456.0F), new Point(123, 456));
    }
}
