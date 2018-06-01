package io.appium.espressoserver.lib.helpers.w3c.dispatcher;

import io.appium.espressoserver.lib.helpers.w3c.adapter.DispatcherAdapter;
import io.appium.espressoserver.lib.helpers.w3c.models.ActionObject;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

public class KeyDown {

    public boolean dispatch(DispatcherAdapter dispatcherAdapter,
            String sourceId, ActionObject object, KeyInputState inputState, long tickDuration) {
        String rawKey = object.getValue();
        String key = "a"; // TODO: Make this normalized key
        boolean repeat = inputState.isPressed(key);
        inputState.addPressed(key);
        boolean alt = inputState.isAlt();

        KeyDownEvent keyDownEvent = new KeyDownEvent(
            key,
        );
        return true;
    }

    public static class KeyDownEvent {
        private String key;
        private char code;
        private int location;
        private boolean altKey;
        private boolean shiftKey;
        private boolean ctrlKey;
        private boolean metaKey;
        private boolean repeat;
        private boolean isComposing;
        private char charCode;
        private char keyCode;
        private char which;
        
        public KeyDownEvent(
                String key,
                char code,
                int location,
                boolean altKey,
                boolean shiftKey,
                boolean ctrlKey,
                boolean metaKey,
                boolean repeat,
                boolean isComposing,
                char charCode,
                char keyCode,
                char which
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
            this.charCode = charCode;
            this.keyCode = keyCode;
            this.which = which;
        }
    }
}
