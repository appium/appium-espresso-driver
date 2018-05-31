package io.appium.espressoserver.lib.helpers.w3c.models;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;

public class ActionObject {
    private InputSourceType type;
    private ActionType subType;
    private String id;
    private Long duration;
    private String origin;
    private Long x;
    private Long y;
    private int button;

    public ActionObject(String id, InputSourceType type, Action action, int index){
        this.type = type;
        this.subType = action.getType();
    }

    public InputSourceType getType() {
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }

    public ActionType getSubType() {
        return subType;
    }

    public void setSubType(ActionType subType) {
        this.subType = subType;
    }

    @Nullable
    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Nullable
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Nullable
    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    @Nullable
    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }
}
