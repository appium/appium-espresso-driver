package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyCodeMapper;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyLocationMapper;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.InputStateTable;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

import static io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP;

/**
 * Implement key dispatch events (https://www.w3.org/TR/webdriver/#keyboard-actions)
 */
public class KeyDispatch {

    private static KeyEvent dispatchKeyEvent(W3CActionAdapter dispatcherAdapter,
                                             ActionObject actionObject,
                                             KeyInputState inputState,
                                             InputStateTable inputStateTable,
                                             long tickDuration, boolean down) throws AppiumException {
        // Get the base Key Event
        KeyEvent keyEvent = getKeyEvent(dispatcherAdapter, actionObject);
        String key = keyEvent.getKey();

        // 3. If the input state's pressed property contains key, let repeat be true, otherwise let repeat be false.
        boolean repeat = inputState.isPressed(key);

        // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
        // keyCode and which properties.
        // Omiting this for now until there's an implementation that needs it.

        // 7-10: Set the alt, shift, ctrl meta key state
        if (down) {
            inputState.setAlt(inputState.isAlt() || key.equals(NormalizedKeys.ALT));
            inputState.setShift(inputState.isShift() || key.equals(NormalizedKeys.SHIFT));
            inputState.setCtrl(inputState.isCtrl() || key.equals(NormalizedKeys.CONTROL));
            inputState.setMeta(inputState.isMeta() || key.equals(NormalizedKeys.META));
        } else {
            inputState.setAlt(inputState.isAlt() && !key.equals(NormalizedKeys.ALT));
            inputState.setAlt(inputState.isShift() && !key.equals(NormalizedKeys.SHIFT));
            inputState.setAlt(inputState.isCtrl() && !key.equals(NormalizedKeys.CONTROL));
            inputState.setAlt(inputState.isMeta() && !key.equals(NormalizedKeys.META));
        }

        // 11: Add key to the set corresponding to input state's pressed property
        if (down) {
            inputState.addPressed(key);
        } else {
            inputState.removePressed(key);
        }

        // Must lock the dispatcherAdapter in-case other threads are also using it
        dispatcherAdapter.lockAdapter();
        try {
            if (down) {

                // 12: Append action object with subtype property changed to 'keyUp' to cancel list
                ActionObject cancelActionObject = new ActionObject(actionObject);
                cancelActionObject.setSubType(KEY_UP);
                inputStateTable.addActionToCancel(cancelActionObject);

                // 13: Call implementation specific key-down event
                dispatcherAdapter.keyDown(keyEvent);
            } else {
                // 13: Call implementation specific key-up event
                dispatcherAdapter.keyUp(keyEvent);
            }
        } finally {
            dispatcherAdapter.unlockAdapter();
        }

        return keyEvent;
    }

    /**
     * Dispatch a key down event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
     *
     * @param dispatcherAdapter Adapter that has implementation specific keyDown event
     * @param actionObject W3C Action Object
     * @param inputState State of the input source
     * @param inputStateTable All of the input states for this session
     * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
     * @return KeyEvent (for testing purposes). 'null' if the dispatch failed.
     */
    @Nullable
    public static KeyEvent dispatchKeyDown(W3CActionAdapter dispatcherAdapter,
                                            ActionObject actionObject, KeyInputState inputState,
                                            InputStateTable inputStateTable, long tickDuration) throws AppiumException {

        return dispatchKeyEvent(dispatcherAdapter, actionObject, inputState, inputStateTable ,tickDuration, true);
    }

    @Nullable
    public static KeyEvent dispatchKeyDown(W3CActionAdapter dispatcherAdapter,
                                           ActionObject actionObject, KeyInputState inputState,
                                           InputStateTable inputStateTable) throws AppiumException {
        return dispatchKeyDown(dispatcherAdapter, actionObject, inputState, inputStateTable, 0);
    }

    /**
     * Dispatch a key up event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
     *
     * @param dispatcherAdapter Adapter that has implementation specific keyDown event
     * @param actionObject W3C Action Object
     * @param inputState State of the input source
     * @param inputStateTable All of the input states for this session
     * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
     * @return KeyEvent (for testing purposes). 'null' if the dispatch failed.
     */
    @Nullable
    public static KeyEvent dispatchKeyUp(W3CActionAdapter dispatcherAdapter,
                                         ActionObject actionObject, KeyInputState inputState,
                                         InputStateTable inputStateTable, long tickDuration) throws AppiumException {
        return dispatchKeyEvent(dispatcherAdapter, actionObject, inputState, inputStateTable, tickDuration, false);
    }

    @Nullable
    public static KeyEvent dispatchKeyUp(W3CActionAdapter dispatcherAdapter,
                                         ActionObject actionObject, KeyInputState inputState,
                                         InputStateTable inputStateTable) throws AppiumException {
        return dispatchKeyUp(dispatcherAdapter, actionObject, inputState, inputStateTable, 0);
    }

    /**
     * Helper method to get a Key Event that has common attributes between KeyUp and KeyDispatch
     * @param actionObject Action Object to get key event info for
     * @return KeyEvent Key event info used by adapter
     */
    private static KeyEvent getKeyEvent(W3CActionAdapter dispatcherAdapter, ActionObject actionObject) {
        // 1. Let raw key be action's value property
        String rawKey = actionObject.getValue();

        // 2. Let key be equal to the normalised key value for raw key
        String key = KeyNormalizer.getInstance().getNormalizedKey(rawKey);

        // 4. Let code be 'code' for raw keyp
        String code = KeyCodeMapper.getCode(rawKey);

        // 5. Let location be the key location for raw key
        int location = KeyLocationMapper.getLocation(rawKey);

        // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
        // keyCode and which properties appropriate for a key with key key and location location on
        // a 102 key US keyboard
        int keyCode = dispatcherAdapter.getKeyCode(key, location);
        int charCode = dispatcherAdapter.getCharCode(key, location);
        int which = dispatcherAdapter.getWhich(key, location);

        KeyEvent keyEvent = new KeyEvent();
        keyEvent.setKey(key);
        keyEvent.setCode(code);
        keyEvent.setLocation(location);
        keyEvent.setKeyCode(keyCode);
        keyEvent.setCharCode(charCode);
        keyEvent.setWhich(which);
        return keyEvent;
    }

    public static class KeyEvent {
        private String key;
        private String code;
        private int location;
        private int keyCode;
        private int charCode;
        private int which;
        private boolean altKey;
        private boolean shiftKey;
        private boolean ctrlKey;
        private boolean metaKey;
        private boolean repeat;
        private boolean isComposing = false;

        public KeyEvent(
                String key,
                String code,
                int location,
                int keyCode,
                int charCode,
                int which,
                boolean altKey,
                boolean shiftKey,
                boolean ctrlKey,
                boolean metaKey,
                boolean repeat,
                boolean isComposing
        ) {
            this.key = key;
            this.code = code;
            this.location = location;
            this.keyCode = keyCode;
            this.charCode = charCode;
            this.which = which;
            this.altKey = altKey;
            this.shiftKey = shiftKey;
            this.ctrlKey = ctrlKey;
            this.metaKey = metaKey;
            this.repeat = repeat;
            this.isComposing = isComposing;
        }

        public KeyEvent() { }

        public String logMessage() {
            return String.format(
                "key=[%s] code=[%s] altKey=[%s] shiftKey=[%s] ctrlKey=[%s] metaKey=[%s]",
                key, code, altKey, shiftKey, ctrlKey, metaKey
            );
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }

        public boolean isAltKey() {
            return altKey;
        }

        public void setAltKey(boolean altKey) {
            this.altKey = altKey;
        }

        public boolean isShiftKey() {
            return shiftKey;
        }

        public void setShiftKey(boolean shiftKey) {
            this.shiftKey = shiftKey;
        }

        public boolean isCtrlKey() {
            return ctrlKey;
        }

        public void setCtrlKey(boolean ctrlKey) {
            this.ctrlKey = ctrlKey;
        }

        public boolean isMetaKey() {
            return metaKey;
        }

        public void setMetaKey(boolean metaKey) {
            this.metaKey = metaKey;
        }

        public boolean isRepeat() {
            return repeat;
        }

        public void setRepeat(boolean repeat) {
            this.repeat = repeat;
        }

        public boolean isComposing() {
            return isComposing;
        }

        public void setComposing(boolean composing) {
            isComposing = composing;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }

        public int getCharCode() {
            return charCode;
        }

        public void setCharCode(int charCode) {
            this.charCode = charCode;
        }

        public int getWhich() {
            return which;
        }

        public void setWhich(int which) {
            this.which = which;
        }
    }
}
