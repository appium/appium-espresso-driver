package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException;
import io.appium.espressoserver.lib.helpers.w3c.adapter.W3CActionAdapter;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyCodeMapper;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyLocationMapper;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.KeyNormalizer;
import io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants.NormalizedKeys;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

/**
 * Implement key dispatch events (https://www.w3.org/TR/webdriver/#keyboard-actions)
 */
public class KeyDispatch {

    /**
     * Dispatch a key down event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
     *
     * @param dispatcherAdapter Adapter that has implementation specific keyDown event
     * @param actionObject W3C Action Object
     * @param inputState State of the input source
     * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
     * @return KeyEvent (for testing purposes). 'null' if the dispatch failed.
     */
    @Nullable
    public static KeyEvent dispatchKeyDown(W3CActionAdapter dispatcherAdapter,
                                   ActionObject actionObject, KeyInputState inputState, long tickDuration) throws AppiumException {

        // Get the base Key Event
        KeyEvent keyEvent = getKeyEvent(actionObject);
        String key = keyEvent.getKey();

        // 3. If the input state's pressed property contains key, let repeat be true, otherwise let repeat be false.
        boolean repeat = inputState.isPressed(key);

        // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
        // keyCode and which properties.
        // Omiting this for now until there's an implementation that needs it.

        // 7-10: Set the alt, shift, ctrl meta key state
        boolean alt = key.equals(NormalizedKeys.ALT);
        boolean shift = key.equals(NormalizedKeys.SHIFT);
        boolean ctrl = key.equals(NormalizedKeys.CONTROL);
        boolean meta = key.equals(NormalizedKeys.META);
        if (alt) inputState.setAlt(true);
        if (shift) inputState.setShift(true);
        if (ctrl) inputState.setCtrl(true);
        if (meta) inputState.setMeta(true);

        // 11: Add key to the set corresponding to input state's pressed property
        inputState.addPressed(key);

        // 12. Call implementation specific key-down event
        // Must lock the dispatcherAdapter in-case other threads are also using it
        dispatcherAdapter.lockAdapter();
        try {
            boolean success = dispatcherAdapter.keyDown(keyEvent);
            if (!success) {
                return null;
            }
        } finally {
            dispatcherAdapter.unlockAdapter();
        }

        return keyEvent;
    }

    @Nullable
    public static KeyEvent dispatchKeyDown(W3CActionAdapter dispatcherAdapter,
                                           ActionObject actionObject, KeyInputState inputState) throws AppiumException {
        return dispatchKeyDown(dispatcherAdapter, actionObject, inputState, 0);
    }

    /**
     * Dispatch a key up event (in accordance with https://www.w3.org/TR/webdriver/#keyboard-actions)
     *
     * @param dispatcherAdapter Adapter that has implementation specific keyDown event
     * @param actionObject W3C Action Object
     * @param inputState State of the input source
     * @param tickDuration How long the 'tick' is. This is unused currently but may be used in the future
     * @return KeyEvent (for testing purposes). 'null' if the dispatch failed.
     */
    @Nullable
    public static KeyEvent dispatchKeyUp(W3CActionAdapter dispatcherAdapter,
                                         ActionObject actionObject, KeyInputState inputState, long tickDuration) throws AppiumException {
        // Get the base Key Event
        KeyEvent keyEvent = getKeyEvent(actionObject);
        String key = keyEvent.getKey();

        // 3. If the input state's pressed property does not contain key, return.
        if(!inputState.isPressed(key)) {
            return keyEvent;
        }

        // 6. Let charCode, keyCode and which be the implementation-specific values of the charCode,
        // keyCode and which properties.
        // Omiting this for now until there's an implementation that needs it.

        // 7-10: Set the alt, shift, ctrl meta key state
        boolean alt = key.equals(NormalizedKeys.ALT);
        boolean shift = key.equals(NormalizedKeys.SHIFT);
        boolean ctrl = key.equals(NormalizedKeys.CONTROL);
        boolean meta = key.equals(NormalizedKeys.META);
        if (alt) inputState.setAlt(false);
        if (shift) inputState.setShift(false);
        if (ctrl) inputState.setCtrl(false);
        if (meta) inputState.setMeta(false);

        // 11: Remove key from the set corresponding to input state's pressed property.
        inputState.removePressed(key);

        boolean success = dispatcherAdapter.keyUp(keyEvent);

        if (!success) {
            return null;
        }

        return keyEvent;
    }

    @Nullable
    public static KeyEvent dispatchKeyUp(W3CActionAdapter dispatcherAdapter,
                                         ActionObject actionObject, KeyInputState inputState) throws AppiumException {
        return dispatchKeyUp(dispatcherAdapter, actionObject, inputState, 0);
    }

    /**
     * Helper method to get a Key Event that has common attributes between KeyUp and KeyDispatch
     * @param actionObject Action Object to get key event info for
     * @return KeyEvent Key event info used by adapter
     * @return
     */
    private static KeyEvent getKeyEvent(ActionObject actionObject) {
        // 1. Let raw key be action's value property
        String rawKey = actionObject.getValue();

        // 2. Let key be equal to the normalised key value for raw key
        String key = KeyNormalizer.getInstance().getNormalizedKey(rawKey);

        // 4. Let code be 'code' for raw keyp
        String code = KeyCodeMapper.getCode(rawKey);

        // 5. Let location be the key location for raw key
        int location = KeyLocationMapper.getLocation(rawKey);

        KeyEvent keyEvent = new KeyEvent();
        keyEvent.setKey(key);
        keyEvent.setCode(code);
        keyEvent.setLocation(location);
        return keyEvent;
    }

    public static class KeyEvent {
        private String key;
        private String code;
        private int location;
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
            this.altKey = altKey;
            this.shiftKey = shiftKey;
            this.ctrlKey = ctrlKey;
            this.metaKey = metaKey;
            this.repeat = repeat;
            this.isComposing = isComposing;
        }

        public KeyEvent() { }

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
    }
}
