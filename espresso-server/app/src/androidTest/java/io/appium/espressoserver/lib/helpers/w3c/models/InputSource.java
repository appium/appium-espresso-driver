/**
 * InputSource
 *
 * (refer to https://www.w3.org/TR/webdriver/#terminology-0 of W3C spec)
 *
 * Represents a Virtual Device providing input events
 */
package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class InputSource {
    private final String VIEWPORT = "viewport";
    private final String POINTER = "pointer";

    private InputSourceType type;
    private String id;
    private Parameters parameters;
    private List<Action> actions;

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
        Long duration; // time in milliseconds
        String origin; // origin; could be viewport, pointer or <ELEMENT_ID>
        Integer button; // Button that is being pressed. Defaults to 0.
        Long x; // x coordinate of pointer
        Long y; // y coordinate of pointer
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
        public Long getDuration(){
            return duration;
        }

        public void setDuration(long duration){
            this.duration = duration;
        }

        public String getOrigin(){
            if (origin == null) {
                return VIEWPORT;
            }
            return origin;
        }

        public void setOrigin(String origin){
            this.origin = origin;
        }

        public int getButton(){
            if (button == null) {
                return 0;
            }
            return button;
        }

        public void setButton(int button){
            this.button = button;
        }

        public boolean isOriginViewport(){
            return origin.toLowerCase().equals(VIEWPORT);
        }

        public boolean isOriginPointer(){
            return origin.toLowerCase().equals(POINTER);
        }

        @Nullable
        public Long getX(){
            return x;
        }

        public void setX(long x){
            this.x = x;
        }

        @Nullable
        public Long getY(){
            return y;
        }

        public void setY(long y){
            this.y = y;
        }

        @Nullable
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

        PointerType getPointerType(){
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
