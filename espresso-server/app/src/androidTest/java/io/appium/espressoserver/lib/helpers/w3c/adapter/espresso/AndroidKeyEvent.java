package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;
import io.appium.espressoserver.lib.helpers.AndroidLogger;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.W3CKeyEvent;

import static android.view.KeyEvent.*;
import static io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys.*;

public class AndroidKeyEvent {

    private final KeyCharacterMap keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    private final UiController uiController;
    private Map<String, List<KeyEvent>> keyDownTimes = new HashMap<>();

    public AndroidKeyEvent(UiController uiController) {
        this.uiController = uiController;
    }

    public void keyDown(final W3CKeyEvent w3cKeyEvent) throws AppiumException {
        keyUpOrDown(w3cKeyEvent, true);
    }

    public void keyUp(final W3CKeyEvent w3cKeyEvent) throws AppiumException {
        keyUpOrDown(w3cKeyEvent, false);
    }

    /**
     * Translate a string into a sequence of Android Key Events
     * @param key A key in String form
     * @param isDown Returns true if this is a key down events
     * @param isRepeat Returns true if this is a repeat key down
     * @param metaState The current global meta state of Android key events
     * @return
     * @throws InvalidArgumentException
     */
    private List<KeyEvent> convertStringToAndroidKeyEvents(String key, boolean isDown,
                                                           boolean isRepeat, int metaState) throws InvalidArgumentException {
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
                    keyDownTimes.get(key).get(keyEventIndex).getDownTime();

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

    /**
     * Dispatch an Android key event
     * @param w3cKeyEvent Key Event in W3C form (https://www.w3.org/TR/webdriver1/#keyboard-actions)
     * @param isDown Returns true if it's keyDown, otherwise it's keyUp
     * @throws AppiumException
     */
    private void keyUpOrDown(final W3CKeyEvent w3cKeyEvent, boolean isDown)
            throws AppiumException {

        int action = isDown ? ACTION_DOWN : ACTION_UP;
        String key = w3cKeyEvent.getKey();
        int keyCode = w3cKeyEvent.getKeyCode();

        // No-op if releasing a key that isn't down
        if (action == ACTION_UP && keyDownTimes.containsKey(key)) {
            return;
        }

        List<KeyEvent> keyEvents;

        if (keyCode >= 0) {

            // If the keyCode is known, send it now
            long downTime = isDown ?
                    SystemClock.uptimeMillis() :
                    keyDownTimes.get(key).get(0).getDownTime();
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
            keyEvents = convertStringToAndroidKeyEvents(key, isDown, w3cKeyEvent.isRepeat(), getMetaState(w3cKeyEvent));
        }

        injectKeyEvents(isDown, keyEvents, key);
    }

    /**
     * Translate a W3C Key into an Android Key Event (if possible
     * @param keyValue The W3C normalized key value (https://www.w3.org/TR/webdriver1/#keyboard-actions)
     * @param location The W3C "key location"
     * @return
     */
    private static int getKeyCode(String keyValue, int location) {
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

    /**
     * Get the Android key meta state from a W3C key event
     * @param keyEvent W3C key event
     * @return
     */
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


    /**
     * Inject a key event into UiController
     * @param isDown Is it a key down event?
     * @param keyEvents A list of the Android Key Events to inject
     * @param key The key to inject in string form (for logging purposes)
     * @throws AppiumException
     */
    private void injectKeyEvents(boolean isDown, List<KeyEvent> keyEvents, String key) throws AppiumException {
        // If it's a keydown event, record that this key went down. If it's keyup, remove pre-existing
        // record of this key going down
        if (isDown) {
            keyDownTimes.put(key, keyEvents);
        } else {
            keyDownTimes.remove(key);
        }

        // Inject all of the key events, in order
        for (final KeyEvent androidKeyEvent : keyEvents){
            AndroidLogger.logger.info(String.format("Calling key event: %s", androidKeyEvent));
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
}
