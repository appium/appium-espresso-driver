package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Key input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
public class KeyInputState implements InputStateInterface {
    private Set<String> pressed;
    private boolean alt;
    private boolean shift;
    private boolean ctrl;
    private boolean meta;

    public KeyInputState() {
        pressed = new HashSet<>();
        alt = false;
        shift = false;
        ctrl = false;
        meta = false;
    }


    public boolean isPressed(String key) {
        return pressed.contains(key);
    }

    public void addPressed(String key) {
        pressed.add(key);
    }

    public void removePressed(String key) {
        pressed.remove(key);
    }

    public boolean isAlt() {
        return alt;
    }

    public void setAlt(boolean alt) {
        this.alt = alt;
    }

    public boolean isShift() {
        return shift;
    }

    public void setShift(boolean shift) {
        this.shift = shift;
    }

    public boolean isCtrl() {
        return ctrl;
    }

    public void setCtrl(boolean ctrl) {
        this.ctrl = ctrl;
    }

    public boolean isMeta() {
        return meta;
    }

    public void setMeta(boolean meta) {
        this.meta = meta;
    }

    /**
     * Implement 'calculated global key state' in spec 17.2
     */
    public static KeyInputState getAggregateKeyInputState(List<KeyInputState> keyInputStates) {
        Set<String> pressed = new HashSet<>();
        boolean isAlt = false;
        boolean isShift = false;
        boolean isCtrl = false;
        boolean isMeta = false;

        for (KeyInputState keyInputState:keyInputStates) {
            if(keyInputState.isAlt()) {
                isAlt = true;
            }
            if(keyInputState.isShift()) {
                isShift = true;
            }
            if(keyInputState.isCtrl()) {
                isCtrl = true;
            }
            if(keyInputState.isMeta()) {
                isMeta = true;
            }
            pressed.addAll(keyInputState.pressed);
        }

        KeyInputState outputState = new KeyInputState();
        outputState.setAlt(isAlt);
        outputState.setShift(isShift);
        outputState.setCtrl(isCtrl);
        outputState.setMeta(isMeta);
        outputState.pressed = pressed;

        return outputState;
    }
}
