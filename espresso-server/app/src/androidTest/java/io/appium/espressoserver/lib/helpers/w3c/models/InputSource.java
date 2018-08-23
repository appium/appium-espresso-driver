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
    public static final String ELEMENT = "element-6066-11e4-a52e-4f735466cecf";

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
                return new PointerInputState(getPointerType());
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

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
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
        private String value; // a string containing a single Unicode code point or a number
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
        public String getValue(){
            return value;
        }

        public void setValue(String value){
            this.value = value;
        }

        public Origin getOrigin() {
            return origin;
        }

        public void setOrigin(Origin origin) {
            this.origin = origin;
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

    public static class InputSourceBuilder {

        private InputSourceType type;
        private String id;
        private Parameters parameters;
        private List<Action> actions;

        public InputSourceBuilder withType(InputSourceType type) {
            this.type = type;
            return this;
        }

        public InputSourceBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public InputSourceBuilder withParameters(Parameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public InputSourceBuilder withActions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        public InputSource build() {
            InputSource inputSource = new InputSource();
            inputSource.setActions(actions);
            inputSource.setType(type);
            inputSource.setId(id);
            inputSource.setParameters(parameters);
            return inputSource;
        }

    }

    public static class ActionBuilder {
        private ActionType type;
        private Long duration;
        private Integer button;
        private Long x;
        private Long y;
        private String value;
        private Origin origin = new Origin();

        public ActionBuilder withType(ActionType type) {
            this.type = type;
            return this;
        }

        public ActionBuilder withDuration(Long duration) {
            this.duration = duration;
            return this;
        }

        public ActionBuilder withButton(Integer button) {
            this.button = button;
            return this;
        }

        public ActionBuilder withX(Long x) {
            this.x = x;
            return this;
        }

        public ActionBuilder withY(Long y) {
            this.y = y;
            return this;
        }

        public ActionBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public ActionBuilder withOrigin(Origin origin) {
            this.origin = origin;
            return this;
        }

        public ActionBuilder withOrigin(String originType) {
            this.origin.setType(originType);
            return this;
        }

        public ActionBuilder withElementId(String elementId) {
            this.origin.setType(InputSource.ELEMENT);
            this.origin.setElementId(elementId);
            return this;
        }

        public Action build() {
            Action action = new Action();
            if (duration != null) {
                action.setDuration(duration);
            }
            action.setType(type);
            if (button != null) {
                action.setButton(button);
            }
            action.setValue(value);
            if (x != null) {
                action.setX(x);
            }
            if (y != null) {
                action.setY(y);
            }
            action.setOrigin(origin);
            return action;
        }
    }
}
