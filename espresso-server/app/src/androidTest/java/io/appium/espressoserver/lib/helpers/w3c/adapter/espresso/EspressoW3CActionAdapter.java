package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.graphics.Point;
import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException;
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException;
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.Logger;
import io.appium.espressoserver.lib.helpers.w3c.adapter.BaseW3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.model.Element;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_ALT_LEFT;
import static android.view.KeyEvent.KEYCODE_BREAK;
import static android.view.KeyEvent.KEYCODE_CLEAR;
import static android.view.KeyEvent.KEYCODE_COMMA;
import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_EQUALS;
import static android.view.KeyEvent.KEYCODE_ESCAPE;
import static android.view.KeyEvent.KEYCODE_F1;
import static android.view.KeyEvent.KEYCODE_F10;
import static android.view.KeyEvent.KEYCODE_F11;
import static android.view.KeyEvent.KEYCODE_F12;
import static android.view.KeyEvent.KEYCODE_F2;
import static android.view.KeyEvent.KEYCODE_F3;
import static android.view.KeyEvent.KEYCODE_F4;
import static android.view.KeyEvent.KEYCODE_F5;
import static android.view.KeyEvent.KEYCODE_F6;
import static android.view.KeyEvent.KEYCODE_F7;
import static android.view.KeyEvent.KEYCODE_F8;
import static android.view.KeyEvent.KEYCODE_F9;
import static android.view.KeyEvent.KEYCODE_FORWARD_DEL;
import static android.view.KeyEvent.KEYCODE_HELP;
import static android.view.KeyEvent.KEYCODE_HOME;
import static android.view.KeyEvent.KEYCODE_INSERT;
import static android.view.KeyEvent.KEYCODE_META_LEFT;
import static android.view.KeyEvent.KEYCODE_MINUS;
import static android.view.KeyEvent.KEYCODE_MOVE_END;
import static android.view.KeyEvent.KEYCODE_NUMPAD_ADD;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static android.view.KeyEvent.KEYCODE_PERIOD;
import static android.view.KeyEvent.KEYCODE_SEMICOLON;
import static android.view.KeyEvent.KEYCODE_SHIFT_LEFT;
import static android.view.KeyEvent.KEYCODE_SLASH;
import static android.view.KeyEvent.KEYCODE_SPACE;
import static android.view.KeyEvent.KEYCODE_STAR;
import static android.view.KeyEvent.KEYCODE_TAB;
import static android.view.KeyEvent.KEYCODE_UNKNOWN;
import static android.view.KeyEvent.KEYCODE_ZENKAKU_HANKAKU;
import static android.view.KeyEvent.META_ALT_MASK;
import static android.view.KeyEvent.META_CTRL_MASK;
import static android.view.KeyEvent.META_SHIFT_MASK;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ALT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_DOWN;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_LEFT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_RIGHT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ARROW_UP;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ASTERISK;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.BACKSPACE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.CLEAR;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.COMMA;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.CONTROL;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.DELETE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.EIGHT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.END;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.EQUALS;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ESCAPE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F1;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F10;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F11;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F12;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F2;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F3;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F4;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F5;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F6;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F7;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F8;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.F9;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FIVE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FORWARD_SLASH;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.FOUR;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HELP;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HOME;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.HYPHEN;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.INSERT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.META;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.NINE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ONE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAGEDOWN;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAGEUP;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PAUSE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PERIOD;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.PLUS;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.RETURN;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SEMICOLON;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SEVEN;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SHIFT;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.SIX;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.TAB;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.THREE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.TWO;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.UNIDENTIFIED;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.WHITESPACE;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ZENKAKU_HANKAKU;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.ZERO;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.MOUSE;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class EspressoW3CActionAdapter extends BaseW3CActionAdapter {

    private final boolean isTouchScreen;
    private UiController uiController;
    private final MultiTouchState multiTouchState = new MultiTouchState();
    private Map<String, List<KeyEvent>> keyDownEvents = new HashMap<>();
    private final KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    private final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

    public EspressoW3CActionAdapter(UiController uiController) {
        this.isTouchScreen = isTouchScreen();
        this.uiController = uiController;
    }

    public void keyDown(final W3CKeyEvent keyEvent) throws AppiumException {
        keyUpOrDown(keyEvent, true);
    }

    public void keyUp(final W3CKeyEvent keyEvent) throws AppiumException {
        if (keyDownEvents.containsKey(keyEvent.getKey())) {
            keyUpOrDown(keyEvent, false);
        }
    }

    private void keyUpOrDown(final W3CKeyEvent w3cKeyEvent, boolean isDown) throws AppiumException {
        int action = isDown ? ACTION_DOWN : ACTION_UP;
        String key = w3cKeyEvent.getKey();
        int keyCode = w3cKeyEvent.getKeyCode();

        List<KeyEvent> keyEvents;

        if (keyCode >= 0) {

            // If the keyCode is known, send it now
            long downTime = isDown ?
                    SystemClock.uptimeMillis() :
                    keyDownEvents.get(key).get(0).getDownTime();
            keyEvents = Collections.singletonList(new KeyEvent(
                    downTime,
                    isDown ? downTime : SystemClock.uptimeMillis(),
                    action,
                    keyCode,
                    w3cKeyEvent.isRepeat() ? 1 : 0,
                    getMetaState(w3cKeyEvent),
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
            ));
        } else {
            // If there's no keyCode, map the characters to Android keys
            keyEvents = getKeyEventsFromString(key, isDown, w3cKeyEvent.isRepeat(), getMetaState(w3cKeyEvent));
        }

        injectKeyEvents(uiController, isDown, key, keyEvents);
    }

    private List<KeyEvent> getKeyEventsFromString(String key, boolean isDown, boolean isRepeat, int metaState) throws InvalidArgumentException {
        final KeyEvent[] keyEventsFromChar = keyCharacterMap.getEvents(key.toCharArray());

        if (keyEventsFromChar == null) {
            throw new InvalidArgumentException(String.format("Could not find matching keycode for character %s", key));
        }

        long now = SystemClock.uptimeMillis();

        List<KeyEvent> keyEvents = new ArrayList<>();
        int keyEventIndex = 0;
        while (keyEventIndex * 2 < keyEventsFromChar.length) {
            // getEvents produces UP and DOWN events, so we need to skip over every other event
            final KeyEvent keyEvent = keyEventsFromChar[keyEventIndex * 2];

            long downTime = isDown ?
                    SystemClock.uptimeMillis() :
                    keyDownEvents.get(key).get(keyEventIndex).getDownTime();

            keyEvents.add(new KeyEvent(
                    downTime,
                    isDown ? downTime : now,
                    isDown ? ACTION_DOWN : ACTION_UP,
                    keyEvent.getKeyCode(),
                    isRepeat ? 1 : 0,
                    metaState | keyEvent.getMetaState(),
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
            ));

            keyEventIndex++;
        }

        return keyEvents;
    }

    private void injectKeyEvents(UiController uiController, boolean isDown, String key, List<KeyEvent> keyEvents) throws AppiumException {
        getLogger().info(String.format("Calling key %s event on character: %s",
                isDown ? "down" : "up",
                key
        ));

        // Save references to KeyEvents
        if (isDown) {
            keyDownEvents.put(key, keyEvents);
        } else {
            keyDownEvents.remove(key);
        }

        for (final KeyEvent androidKeyEvent : keyEvents){
            boolean isSuccess;
            try {
                isSuccess = uiController.injectKeyEvent(androidKeyEvent);
            } catch (InjectEventSecurityException e) {
                throw new AppiumException(e.getCause().toString());
            }
            if (!isSuccess) {
                throw new AppiumException(String.format("Could not inject key event %s", key));
            }
        }
    }

    private int getMetaState(final W3CKeyEvent keyEvent) {
        int metaState = 0;

        if (keyEvent.isAltKey()) {
            metaState |= META_ALT_MASK;
        }
        if (keyEvent.isCtrlKey()) {
            metaState |= META_CTRL_MASK;
        }
        if (keyEvent.isShiftKey()) {
            metaState |= META_SHIFT_MASK;
        }

        return metaState;
    }

    public void pointerDown(int button, String sourceId, PointerType pointerType,
                     Long x, Long y, Set<Integer> depressedButtons,
                     KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer down at coordinates: %s %s %s", x, y, pointerType));

        if (isTouch(pointerType)) {
            // touch down actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_DOWN, sourceId, x, y, globalKeyInputState, button);
        } else {
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            List<Long> xList = Collections.singletonList(x);
            List<Long> yList = Collections.singletonList(y);
            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_DOWN, button, pointerType, globalKeyInputState, null, 0);

            androidMotionEvent.pointerEvent(
                    xList, yList,
                    ACTION_POINTER_DOWN, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerUp(int button, String sourceId, PointerType pointerType,
                   Long x, Long y, Set<Integer> depressedButtons,
                   KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer up at coordinates: %s %s %s", x, y, pointerType));
        if (isTouch(pointerType)) {
            // touch up actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_UP, sourceId, x, y, globalKeyInputState, button);
        } else {
            List<Long> xList = Collections.singletonList(x);
            List<Long> yList = Collections.singletonList(y);
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_POINTER_UP, button, pointerType, globalKeyInputState, null, 0);
            androidMotionEvent.pointerEvent(xList, yList,
                    ACTION_UP, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerMove(String sourceId, PointerType pointerType,
                            long currentX, long currentY, long x, long y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer move at coordinates: %s %s %s", x, y, pointerType));
        if (isTouch(pointerType)) {
            multiTouchState.updateTouchState(ACTION_MOVE, sourceId, x, y, globalKeyInputState, null);
            multiTouchState.pointerMove(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                    .pointerMove(Collections.singletonList(x), Collections.singletonList(y), pointerType, globalKeyInputState, null);
        }
    }

    public void pointerCancel(String sourceId, PointerType pointerType) throws AppiumException {
        if (isTouch(pointerType)) {
            multiTouchState.pointerCancel(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController).pointerCancel();
        }
    }

    public void sychronousTickActionsComplete() throws AppiumException {
        multiTouchState.perform(uiController);
        AndroidLogger.logger.info("Pointer event: Tick complete");
    }

    public int getKeyCode(String keyValue, int location) throws AppiumException {
        // If it's a normalized keyvalue, map it to it's appropriate key code,
        // otherwise just return -1
        switch (keyValue) {
            case UNIDENTIFIED:
                return KEYCODE_UNKNOWN;
            case HELP:
                return KEYCODE_HELP;
            case BACKSPACE:
                return KEYCODE_DEL;
            case TAB:
                return KEYCODE_TAB;
            case CLEAR:
                return KEYCODE_CLEAR;
            case RETURN:
                return KEYCODE_ENTER;
            case PAUSE:
                return KEYCODE_BREAK;
            case ESCAPE:
                return KEYCODE_ESCAPE;
            case WHITESPACE:
                return KEYCODE_SPACE;
            case SEMICOLON:
                return KEYCODE_SEMICOLON;
            case EQUALS:
                return KEYCODE_EQUALS;
            case ZERO:
                return KEYCODE_0;
            case ONE:
                return KEYCODE_1;
            case TWO:
                return KEYCODE_2;
            case THREE:
                return KEYCODE_3;
            case FOUR:
                return KEYCODE_4;
            case FIVE:
                return KEYCODE_5;
            case SIX:
                return KEYCODE_6;
            case SEVEN:
                return KEYCODE_7;
            case EIGHT:
                return KEYCODE_8;
            case NINE:
                return KEYCODE_9;
            case ASTERISK:
                return KEYCODE_STAR;
            case PLUS:
                return KEYCODE_NUMPAD_ADD;
            case COMMA:
                return KEYCODE_COMMA;
            case HYPHEN:
                return KEYCODE_MINUS;
            case PERIOD:
                return KEYCODE_PERIOD;
            case FORWARD_SLASH:
                return KEYCODE_SLASH;
            case F1:
                return KEYCODE_F1;
            case F2:
                return KEYCODE_F2;
            case F3:
                return KEYCODE_F3;
            case F4:
                return KEYCODE_F4;
            case F5:
                return KEYCODE_F5;
            case F6:
                return KEYCODE_F6;
            case F7:
                return KEYCODE_F7;
            case F8:
                return KEYCODE_F8;
            case F9:
                return KEYCODE_F9;
            case F10:
                return KEYCODE_F10;
            case F11:
                return KEYCODE_F11;
            case F12:
                return KEYCODE_F12;
            case ZENKAKU_HANKAKU:
                return KEYCODE_ZENKAKU_HANKAKU;
            case SHIFT:
                return KEYCODE_SHIFT_LEFT;
            case CONTROL:
                return KEYCODE_CTRL_LEFT;
            case ALT:
                return KEYCODE_ALT_LEFT;
            case META:
                return KEYCODE_META_LEFT;
            case PAGEUP:
                return KEYCODE_PAGE_UP;
            case PAGEDOWN:
                return KEYCODE_PAGE_DOWN;
            case END:
                return KEYCODE_MOVE_END;
            case HOME:
                return KEYCODE_HOME;
            case ARROW_LEFT:
                return KEYCODE_DPAD_LEFT;
            case ARROW_UP:
                return KEYCODE_DPAD_DOWN;
            case ARROW_RIGHT:
                return KEYCODE_DPAD_RIGHT;
            case ARROW_DOWN:
                return KEYCODE_DPAD_DOWN;
            case INSERT:
                return KEYCODE_INSERT;
            case DELETE:
                return KEYCODE_FORWARD_DEL;
            default:
                return -1;
        }
    }

    public long getViewportHeight() {
        return displayMetrics.heightPixels;
    }

    public long getViewportWidth() {
        return displayMetrics.widthPixels;
    }

    public Point getElementCenterPoint(String elementId)
            throws NoSuchElementException, StaleElementException, NotYetImplementedException {
        View view = Element.getViewById(elementId);
        int[] out = new int[2];
        view.getLocationInWindow(out);
        Point point = new Point();
        point.x = out[0] + ((view.getWidth()) / 2);
        point.y = out[1] + ((view.getHeight()) / 2);
        return point;
    }

    public void waitForUiThread() {
        uiController.loopMainThreadUntilIdle();
    }

    public void sleep(long duration) throws AppiumException {
        uiController.loopMainThreadForAtLeast(duration);
    }
    
    public Logger getLogger() {
        return AndroidLogger.logger;
    }

    // If a 'mouse' event was provided, but it's a touchscreen, switch to 'touch'
    // This is because some clients only send 'mouse' events and the assumption is that if they
    // send 'mouse' events to a device that has a touch screen, it needs to be converted
    // This may need to be re-visited in the future if we wish to support Android laptops
    private boolean isTouch(PointerType type) {
        return type == TOUCH || (type == MOUSE && isTouchScreen);
    }

    private boolean isTouchScreen() {
        // If we find one deviceId that is a touchscreen, assume it's a touch screen
        for (int deviceId : InputDevice.getDeviceIds()) {
            int sources = InputDevice.getDevice(deviceId).getSources();
            if ((sources & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN) {
                return true;
            }
        }
        return false;
    }
}
