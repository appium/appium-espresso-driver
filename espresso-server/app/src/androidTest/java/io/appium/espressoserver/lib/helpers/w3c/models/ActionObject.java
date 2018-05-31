package io.appium.espressoserver.lib.helpers.w3c.models;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.*;

public class ActionObject {
    private InputSourceType type;
    private ActionType subType;
    private String id;
    private Long duration;
    private String origin;
    private Long x;
    private Long y;

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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }
}
