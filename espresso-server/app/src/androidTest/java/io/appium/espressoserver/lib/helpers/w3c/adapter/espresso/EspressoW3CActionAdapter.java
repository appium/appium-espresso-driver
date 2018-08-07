package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

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

import static android.view.KeyEvent.*;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.*;
import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType.TOUCH;

public class EspressoW3CActionAdapter extends BaseW3CActionAdapter {

    private UiController uiController;
    private final MultiTouchState multiTouchState = new MultiTouchState();
    private Map<String, List<KeyEvent>> keyDownEvents = new HashMap<>();
    private final KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);

    public EspressoW3CActionAdapter(UiController uiController) {
        this.uiController = uiController;
    }

    public void keyDown(final W3CKeyEvent keyEvent) throws AppiumException {
        keyUpOrDown(keyEvent, true);
    }

    public void keyUp(final W3CKeyEvent keyEvent) throws AppiumException {
        keyUpOrDown(keyEvent, false);
    }

    private void keyUpOrDown(final W3CKeyEvent w3cKeyEvent, boolean isDown) throws AppiumException {
        int action = isDown ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP;
        String key = w3cKeyEvent.getKey();
        int keyCode = w3cKeyEvent.getKeyCode();

        List<KeyEvent> keyEvents;

        long now = SystemClock.uptimeMillis();

        if (keyCode >= 0) {

            // If the keyCode is known, send it now
            long downTime = isDown ?
                    SystemClock.uptimeMillis() :
                    keyDownEvents.get(key).get(0).getDownTime();
            keyEvents = Collections.singletonList(new KeyEvent(
                    downTime,
                    isDown ? downTime : now,
                    action,
                    keyCode,
                    w3cKeyEvent.isRepeat() ? 1 : 0,
                    getMetaState(w3cKeyEvent),
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
            ));
        } else {
            // If there's no keyCode, map the characters to Android keys
            // This method produces both DOWN and UP so we have to skip over odd events
            final KeyEvent[] keyEventsFromChar = keyCharacterMap.getEvents(key.toCharArray());

            if (keyEventsFromChar == null) {
                throw new InvalidArgumentException(String.format("Could not find matching keycode for character %s", key));
            }

            int keyEventIndex = 0;
            keyEvents = new ArrayList<>();

            while (keyEventIndex * 2 < keyEventsFromChar.length) {
                // getEvents produces UP and DOWN events, so we need to skip over every other event
                final KeyEvent keyEvent = keyEventsFromChar[keyEventIndex * 2];

                long downTime = isDown ?
                        SystemClock.uptimeMillis() :
                        keyDownEvents.get(key).get(keyEventIndex).getDownTime();

                keyEvents.add(new KeyEvent(
                        downTime,
                        isDown ? downTime : now,
                        action,
                        keyEvent.getKeyCode(),
                        w3cKeyEvent.isRepeat() ? 1 : 0,
                        getMetaState(w3cKeyEvent) | keyEvent.getMetaState(),
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0
                ));

                keyEventIndex++;
            }
        }

        getLogger().info(String.format("Calling key %s event on character: %s",
                isDown ? "down" : "up",
                w3cKeyEvent.getKey()
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

    public int getMetaState(final W3CKeyEvent keyEvent) {
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
        this.getLogger().info(String.format("Running pointer down at coordinates: %s %s", x, y));

        if (pointerType == TOUCH) {
            // touch down actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_DOWN, sourceId, x, y, globalKeyInputState, button);
        } else {
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            List<Long> xList = Collections.singletonList(x);
            List<Long> yList = Collections.singletonList(y);
            androidMotionEvent.pointerUpOrDown(
                    xList, yList,
                    ACTION_DOWN, button, pointerType, globalKeyInputState, null, 0);

            androidMotionEvent.pointerUpOrDown(
                    xList, yList,
                    ACTION_POINTER_DOWN, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerUp(int button, String sourceId, PointerType pointerType,
                   Long x, Long y, Set<Integer> depressedButtons,
                   KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer up at coordinates: %s %s", x, y));
        if (pointerType == TOUCH) {
            // touch up actions need to be grouped together
            multiTouchState.updateTouchState(ACTION_UP, sourceId, x, y, globalKeyInputState, button);
        } else {
            List<Long> xList = Collections.singletonList(x);
            List<Long> yList = Collections.singletonList(y);
            AndroidMotionEvent androidMotionEvent = AndroidMotionEvent.getMotionEvent(sourceId, uiController);
            androidMotionEvent.pointerUpOrDown(xList, yList,
                    ACTION_POINTER_UP, button, pointerType, globalKeyInputState, null, 0);
            androidMotionEvent.pointerUpOrDown(xList, yList,
                    ACTION_UP, button, pointerType, globalKeyInputState, null, 0);
        }
    }

    public void pointerMove(String sourceId, PointerType pointerType,
                            long currentX, long currentY, long x, long y,
                            Set<Integer> buttons, KeyInputState globalKeyInputState) throws AppiumException {
        this.getLogger().info(String.format("Running pointer move at coordinates: %s %s %s", x, y, pointerType));
        if (pointerType == TOUCH) {
            multiTouchState.updateTouchState(ACTION_MOVE, sourceId, x, y, globalKeyInputState, null);
            multiTouchState.pointerMove(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController)
                    .pointerMove(Collections.singletonList(x), Collections.singletonList(y), pointerType, globalKeyInputState, null);
        }
    }

    public void pointerCancel(String sourceId, PointerType pointerType) throws AppiumException {
        if (pointerType == TOUCH) {
            multiTouchState.pointerCancel(uiController);
        } else {
            AndroidMotionEvent.getMotionEvent(sourceId, uiController).pointerCancel();
        }
    }

    public void sychronousTickActionsComplete() throws AppiumException {
        AndroidLogger.logger.info("Pointer event: Tick complete");
        multiTouchState.perform(uiController);
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
        // Stub.
        return Long.MAX_VALUE;
    }

    public long getViewportWidth() {
        // Stub.
        return Long.MAX_VALUE;
    }

    public long[] getElementCenterPoint(String elementId)
            throws NoSuchElementException, StaleElementException, NotYetImplementedException {
        // Stub.
        return new long[] { };
    }

    public void waitForUiThread() {
        uiController.loopMainThreadUntilIdle();
    }

    public void sleep(long duration) throws AppiumException {
        SystemClock.sleep(duration);
    }
    
    public Logger getLogger() {
        return AndroidLogger.logger;
    }
}
