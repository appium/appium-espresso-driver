package io.appium.espressoserver.test.helpers.w3c.adapter;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.LinkedList;
import java.util.List;

import androidx.test.espresso.UiController;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.AndroidKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.KeyDispatch;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.META_ALT_MASK;
import static android.view.KeyEvent.META_SHIFT_MASK;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.BACKSPACE;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class AndroidKeyEventTest {

    public static class MockUiController implements UiController {
        public List<KeyEvent> keyEvents = new LinkedList<>();

        @Override
        public boolean injectMotionEvent(MotionEvent event) {
            return false;
        }

        @Override
        public boolean injectMotionEventSequence(Iterable<MotionEvent> events) {
            return false;
        }

        @Override
        public boolean injectKeyEvent(KeyEvent event) {
            this.keyEvents.add(event);
            return true;
        }

        @Override
        public boolean injectString(String str) {
            return false;
        }

        @Override
        public void loopMainThreadUntilIdle() {
            SystemClock.sleep(100);
        }

        @Override
        public void loopMainThreadForAtLeast(long millisDelay) {
            SystemClock.sleep(millisDelay);
        }
    }

    @Test
    public void keyUpNoop() throws AppiumException {
        // Dispatch an up event that has no corresponding down event and check that the state was unchanged
        W3CKeyEvent w3CKeyEvent = new W3CKeyEvent();
        w3CKeyEvent.setKey("A");
        MockUiController mockUiController = new MockUiController();
        assertEquals(mockUiController.keyEvents.size(), 0);
        (new AndroidKeyEvent(mockUiController)).keyUp(w3CKeyEvent);
        assertEquals(mockUiController.keyEvents.size(), 0);
    }

    @Test
    public void keyDown() throws AppiumException {
        MockUiController mockUiController = new MockUiController();

        // Dispatch a key event
        W3CKeyEvent w3CKeyEvent = KeyDispatch.getKeyEvent(new EspressoW3CActionAdapter(mockUiController), "B");
        w3CKeyEvent.setAltKey(true);
        w3CKeyEvent.setShiftKey(true);
        w3CKeyEvent.setCtrlKey(false);
        (new AndroidKeyEvent(mockUiController)).keyDown(w3CKeyEvent);

        // Check that one was dispatched
        assertEquals(mockUiController.keyEvents.size(), 1);

        // Check the right key event was dispatched
        KeyEvent keyEvent = mockUiController.keyEvents.get(mockUiController.keyEvents.size() - 1);
        keyEvent.getKeyCode();
        assertEquals(keyEvent.getKeyCode(), KEYCODE_B);
        assertEquals(keyEvent.getAction(), ACTION_DOWN);
        assertEquals(keyEvent.getMetaState(), META_SHIFT_MASK | META_ALT_MASK);
    }

    @Test
    public void keyDownAndUp() throws AppiumException {
        MockUiController mockUiController = new MockUiController();
        AndroidKeyEvent androidKeyEvent = new AndroidKeyEvent(mockUiController);

        // Key Down C
        W3CKeyEvent w3CKeyEvent = KeyDispatch.getKeyEvent(new EspressoW3CActionAdapter(mockUiController), "C");
        androidKeyEvent.keyDown(w3CKeyEvent);

        // Check that KEYCODE_C went down
        KeyEvent keyEvent = mockUiController.keyEvents.get(mockUiController.keyEvents.size() - 1);
        keyEvent.getKeyCode();
        assertEquals(keyEvent.getKeyCode(), KEYCODE_C);
        assertEquals(keyEvent.getAction(), ACTION_DOWN);

        // Release Key Code C and check that there's no keys down
        androidKeyEvent.keyUp(w3CKeyEvent);
        keyEvent = mockUiController.keyEvents.get(mockUiController.keyEvents.size() - 1);
        keyEvent.getKeyCode();
        assertEquals(keyEvent.getKeyCode(), KEYCODE_C);
        assertEquals(keyEvent.getAction(), ACTION_UP);
    }

    @Test
    public void getKeyCode() {
        assertEquals(AndroidKeyEvent.getKeyCode(BACKSPACE, 0), KEYCODE_DEL);
        assertEquals(AndroidKeyEvent.getKeyCode("A", 0), -1);
    }
}
