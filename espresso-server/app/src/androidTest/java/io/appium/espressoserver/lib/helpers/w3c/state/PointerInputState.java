package io.appium.espressoserver.lib.helpers.w3c.state;

import java.util.HashSet;
import java.util.Set;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource;

/**
 * Pointer input state specified in 17.2 of spec
 *
 * https://www.w3.org/TR/webdriver/#input-source-state
 */
public class PointerInputState implements InputStateInterface {
    private Set<Integer> pressed = new HashSet<>();
    private int x = 0;
    private int y = 0;

    public boolean isPressed(int num) {
        return pressed.contains(num);
    }

    public void addPressed(int num) {
        pressed.add(num);
    }

    public void removePressed(int num) {
        pressed.remove(num);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
