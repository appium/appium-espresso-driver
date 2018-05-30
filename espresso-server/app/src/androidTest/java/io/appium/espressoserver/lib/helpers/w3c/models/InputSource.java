package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class InputSource {
    InputSourceType type;
    String id;
    Parameters parameters;
    List<Action> actions;

    public String getId() {
        return id;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public InputSourceType getType() {
        if (type == null) {
            return InputSourceType.NONE;
        }
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }

    @Nullable
    public PointerType getPointerType(){
        if(parameters != null) {
            return parameters.getPointerType();
        }
        return null;
    }

    public enum InputSourceType {
        @SerializedName("pointer")
        POINTER,
        @SerializedName("key")
        KEY,
        @SerializedName("none")
        NONE;
    }

    public class Action {
        ActionType type; // type of action
        long duration; // time in milliseconds
        String origin; // origin; could be viewport, pointer or <ELEMENT_ID>
        int button; // Button that is being pressed
        long x; // x coordinate of pointer
        long y; // y coordinate of pointer
        String value; // a string containing a single Unicode code point

        public ActionType getType(){
            if (type == null) {
                return ActionType.PAUSE;
            }
            return type;
        }

        public void setType(ActionType actionType){
            this.type = type;
        }

        @Nullable
        public long getDuration(){
            return duration;
        }

        public void setDuration(long duration){
            this.duration = duration;
        }

        @Nullable
        public String getOrigin(){
            return origin;
        }

        public void setOrigin(String origin){
            this.origin = origin;
        }

        public int getButton(){
            return button;
        }

        public void setButton(int button){
            this.button = button;
        }

        public boolean isOriginViewport(){
            return origin.toLowerCase().equals("viewport");
        }

        public boolean isOriginPointer(){
            return origin.toLowerCase().equals("pointer");
        }

        public long getX(){
            return x;
        }

        public void setX(long x){
            this.x = x;
        }

        public long getY(){
            return y;
        }

        public void setY(long y){
            this.y = y;
        }

        public String getValue(){
            return value;
        }

        public void setValue(String value){
            this.value = value;
        }
    }

    public enum ActionType {
        @SerializedName("pause")
        PAUSE,
        @SerializedName("pointerDown")
        POINTER_DOWN,
        @SerializedName("pointerUp")
        POINTER_UP,
        @SerializedName("pointerMove")
        POINTER_MOVE,
        @SerializedName("pointerCancel")
        POINTER_CANCEL,
        @SerializedName("keyUp")
        KEY_UP,
        @SerializedName("keyDown")
        KEY_DOWN;
    }

    public class Parameters {
        PointerType pointerType;

        public PointerType getPointerType(){
            return pointerType;
        }

        public void setPointerType(PointerType pointerType) {
            this.pointerType = pointerType;
        }
    }

    public enum PointerType {
        @SerializedName("mouse")
        MOUSE,
        @SerializedName("pen")
        PEN,
        @SerializedName("touch")
        TOUCH;
    }
}
