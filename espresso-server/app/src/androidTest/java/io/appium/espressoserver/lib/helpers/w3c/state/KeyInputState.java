package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Key input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
public class KeyInputState implements InputState {
    private final Set<String> pressed = new HashSet<>();
    private boolean alt = false;
    private boolean shift = false;
    private boolean ctrl = false;
    private boolean meta = false;

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

    public String logMessage() {
        return String.format(
                "alt=[%s] shift=[%s] ctrl=[%s] meta=[%s] pressed=[]",
                isAlt(), isShift(), isCtrl(), isMeta(), pressed
        );
    }

    /**
     * Implement 'calculated global key state' in spec 17.2
     */
    public static KeyInputState getGlobalKeyState(List<KeyInputState> keyInputStates) {
        boolean isAlt = false;
        boolean isShift = false;
        boolean isCtrl = false;
        boolean isMeta = false;

        KeyInputState outputState = new KeyInputState();

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
            for (String key:keyInputState.pressed) {
                outputState.addPressed(key);
            }
        }

        outputState.setAlt(isAlt);
        outputState.setShift(isShift);
        outputState.setCtrl(isCtrl);
        outputState.setMeta(isMeta);

        return outputState;
    }

}
