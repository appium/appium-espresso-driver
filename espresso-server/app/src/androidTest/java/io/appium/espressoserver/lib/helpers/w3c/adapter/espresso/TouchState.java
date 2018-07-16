package io.appium.espressoserver.lib.helpers.w3c.adapter.espresso;

import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;

public class TouchState {
    private long x;
    private long y;
    private String sourceId;
    private KeyInputState globalKeyInputState;
    private int button;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public KeyInputState getGlobalKeyInputState() {
        return globalKeyInputState;
    }

    public void setGlobalKeyInputState(KeyInputState globalKeyInputState) {
        this.globalKeyInputState = globalKeyInputState;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }
}
