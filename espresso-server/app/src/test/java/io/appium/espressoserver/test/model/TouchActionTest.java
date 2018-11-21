package io.appium.espressoserver.test.model;

import android.view.ViewConfiguration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.List;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action;
import io.appium.espressoserver.lib.model.TouchAction;
import io.appium.espressoserver.lib.model.TouchAction.ActionType;
import io.appium.espressoserver.lib.model.TouchAction.TouchActionOptions;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.POINTER_MOVE;
import static io.appium.espressoserver.lib.model.TouchAction.ActionType.LONG_PRESS;
import static io.appium.espressoserver.lib.model.TouchAction.ActionType.MOVE_TO;
import static io.appium.espressoserver.lib.model.TouchAction.ActionType.PRESS;
import static io.appium.espressoserver.lib.model.TouchAction.ActionType.TAP;
import static io.appium.espressoserver.test.helpers.w3c.Helpers.assertFloatEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest(ViewConfiguration.class)
public class TouchActionTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void before() {
        PowerMockito.mockStatic(ViewConfiguration.class);
        Mockito.when(ViewConfiguration.getTapTimeout()).thenReturn(10);
        Mockito.when(ViewConfiguration.getLongPressTimeout()).thenReturn(100);
    }

    @Test
    public void shouldConvertMoveTo() throws AppiumException {
        TouchAction touchAction = new TouchAction();
        touchAction.setAction(MOVE_TO);
        TouchActionOptions options = new TouchActionOptions();
        options.setX(100L);
        options.setY(200L);
        touchAction.setOptions(options);
        List<Action> actions = touchAction.toW3CAction();
        assertEquals(actions.get(0).getType(), PAUSE);
        assertEquals(actions.get(1).getType(), PAUSE);
        Action action = actions.get(2);
        assertFloatEquals(action.getX(), 100);
        assertFloatEquals(action.getY(), 200);
        assertEquals(action.getType(), POINTER_MOVE);
        assertTrue(action.isOriginViewport());
    }

    @Test
    public void shouldConvertPress() throws AppiumException {

        ActionType[] actionTypes = new ActionType[]{ TAP, PRESS, LONG_PRESS };

        for (ActionType actionType: actionTypes) {
            TouchAction touchAction = new TouchAction();
            touchAction.setAction(actionType);
            TouchActionOptions options = new TouchActionOptions();
            options.setX(100L);
            options.setY(200L);
            touchAction.setOptions(options);
            List<Action> actions = touchAction.toW3CAction();

            Action moveAction = actions.get(0);
            assertEquals(moveAction.getType(), POINTER_MOVE);
            assertFloatEquals(moveAction.getX(), 100);
            assertFloatEquals(moveAction.getY(), 200);

            Action upAction = actions.get(1);
            assertEquals(upAction.getType(), POINTER_DOWN);
            assertEquals(upAction.getButton(), 0);

            Action waitAction = actions.get(2);
            assertEquals(waitAction.getType(), PAUSE);

            if (actionType == PRESS) {
                assertTrue(waitAction.getDuration() > ViewConfiguration.getTapTimeout());
                assertTrue(waitAction.getDuration() < ViewConfiguration.getLongPressTimeout());
            } else if (actionType == TAP) {
                assertTrue(waitAction.getDuration() < ViewConfiguration.getTapTimeout());
            } else if (actionType == LONG_PRESS) {
                assertTrue(waitAction.getDuration() > ViewConfiguration.getLongPressTimeout());
            }
        }
    }
}
