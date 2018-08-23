package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.HashSet;
import java.util.Set;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;

/**
 * Pointer input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
public class PointerInputState implements InputState {
    private Set<Integer> pressed = new HashSet<>();
    private PointerType type;
    private long x = 0;
    private long y = 0;

    public PointerInputState(PointerType pointerType) {
        this.type = pointerType;
    }

    public boolean isPressed(int num) {
        return pressed.contains(num);
    }

    public void addPressed(int num) {
        pressed.add(num);
    }

    public void removePressed(int num) {
        pressed.remove(num);
    }

    public Set<Integer> getButtons() {
        return pressed;
    }

    public boolean hasPressedButtons() {
        return !pressed.isEmpty();
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public PointerType getType() {
        if (type == null) {
            return PointerType.TOUCH;
        }
        return type;
    }

    public void setType(PointerType type) {
        this.type = type;
    }

    public String logMessage() {
        return String.format(
                "pointer-type=[%s] x=[%s] y=[%s] pressed=[%s]",
                type, x, y, pressed
        );
    }
}
