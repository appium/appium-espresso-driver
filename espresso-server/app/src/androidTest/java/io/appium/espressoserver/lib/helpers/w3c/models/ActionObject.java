package io.appium.espressoserver.lib.helpers.w3c.models;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType;
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.PointerType;

public class ActionObject {
    private int index;
    private InputSourceType type;
    private ActionType subType;
    private String id;
    private Long duration;
    private Long x;
    private Long y;
    private int button;
    private String value;
    private PointerType pointer;
    private Origin origin = new Origin();

    public ActionObject() {

    }

    public ActionObject(String id, InputSourceType type, ActionType subType, int index){
        this.type = type;
        this.subType = subType;
        this.id = id;
        this.index = index; // Store the index of the action for possible future logging issues
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
    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
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

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setPointer(PointerType pointer) {
        this.pointer = pointer;
    }

    public PointerType getPointer() {
        return pointer;
    }

    public String getId() {
        return id;
    }
}
