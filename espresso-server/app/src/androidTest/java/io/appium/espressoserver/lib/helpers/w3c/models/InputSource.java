package io.appium.espressoserver.lib.helpers.w3c.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.helpers.w3c.state.InputState;
import io.appium.espressoserver.lib.helpers.w3c.state.KeyInputState;
import io.appium.espressoserver.lib.helpers.w3c.state.PointerInputState;

/**
 * InputSource
 *
 * (refer to https://www.w3.org/TR/webdriver/#terminology-0 of W3C spec)
 *
 * Represents a Virtual Device providing input events
 */
@SuppressWarnings("unused")
public class InputSource {
    public static final String VIEWPORT = "viewport";
    public static final String POINTER = "pointer";
    public static final String ELEMENT = "element";

    private InputSourceType type;
    private String id;
    private Parameters parameters;
    private List<Action> actions;

    private InputState state;

    public InputSource(){

    }

    public InputSource(InputSourceType type, String id, Parameters parameters, List<Action> actions){
        this.type = type;
        this.id = id;
        this.parameters = parameters;
        this.actions = actions;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Nullable
    public InputSourceType getType() {
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }

    /**
     * Get the initial state of an Input Source
     * @return Get the initial input state (see 17.3 for info on Input State)
     */
    public InputState getDefaultState() {
        switch (getType()) {
            case POINTER:
                return new PointerInputState();
            case KEY:
                return new KeyInputState();
            default:
                return null;
        }
    }


    @Nullable
    public PointerType getPointerType(){
        if (parameters != null) {
            return parameters.getPointerType();
        } else if (type == InputSourceType.POINTER) {
            // NOTE: The spec specifies that the default should be MOUSE. This is an exception
            // because the vast majority of use cases on a mobile device will use TOUCH.
            return PointerType.TOUCH;
        }
        return null;
    }

    public enum InputSourceType {
        @SerializedName("pointer")
        POINTER,
        @SerializedName("key")
        KEY,
        @SerializedName("none")
        NONE
    }

    public static class Action {
        private ActionType type; // type of action
        private Long duration; // time in milliseconds
        private Integer button; // Button that is being pressed. Defaults to 0.
        private Long x; // x coordinate of pointer
        private Long y; // y coordinate of pointer
        private Object value; // a string containing a single Unicode code point or a number
        private Origin origin = new Origin(); // origin; could be viewport, pointer or <{element-6066-11e4-a52e-4f735466cecf: <element-uuid>}>

        // Web element identifier: https://www.w3.org/TR/webdriver/#elements
        // (note: in the Appium case it's not actually a "web" element, it's a native element)
        public static final String ELEMENT_CODE = "element-6066-11e4-a52e-4f735466cecf";

        @Nullable
        public ActionType getType(){
            return type;
        }

        public void setType(ActionType type){
            this.type = type;
        }

        @Nullable
        public Long getDuration(){
            return duration;
        }

        public void setDuration(long duration){
            this.duration = duration;
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
            return origin.getType().equalsIgnoreCase(VIEWPORT);
        }

        public boolean isOriginPointer(){
            return origin.getType().equalsIgnoreCase(POINTER);
        }

        public boolean isOriginElement(){
            return origin.getType().equalsIgnoreCase(ELEMENT_CODE);
        }

        public Long getX(){
            return x;
        }

        public void setX(long x){
            this.x = x;
        }

        public Long getY(){
            return y;
        }

        public void setY(long y){
            this.y = y;
        }

        @Nullable
        public Object getValue(){
            return value;
        }

        public void setValue(Object value){
            this.value = value;
        }

        public Origin getOrigin() {
            return origin;
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
        KEY_DOWN
    }

    public static class Parameters {
        private PointerType pointerType;

        private PointerType getPointerType(){
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
        TOUCH
    }
}
