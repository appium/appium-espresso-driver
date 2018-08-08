package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.MotionEvent.BUTTON_SECONDARY;
import static android.view.MotionEvent.BUTTON_TERTIARY;

public class MotionEventParams extends AppiumParams {

    @SerializedName("element")
    private String elementId;
    private Long x;
    private Long y;
    private Integer button;


    public static final int MOUSE_LEFT = 0;
    public static final int MOUSE_MIDDLE = 1;
    public static final int MOUSE_RIGHT = 2;

    @Nullable
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
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

    public Integer getButton() {
        // Left button is the default
        if (button == null) {
            return MOUSE_LEFT;
        }
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }


    public int getAndroidButtonState() throws InvalidArgumentException {
        return getAndroidButtonState(this.getButton());
    }

    public static int getAndroidButtonState(Integer button) throws InvalidArgumentException {
        if (button == null) {
            return BUTTON_PRIMARY;
        }

        switch (button) {
            case MOUSE_LEFT:
                return BUTTON_PRIMARY;
            case MOUSE_MIDDLE:
                return BUTTON_TERTIARY;
            case MOUSE_RIGHT:
                return BUTTON_SECONDARY;
            default:
                throw new InvalidArgumentException(String.format("'%s' is not a valid button type", button));
        }

    }
}
