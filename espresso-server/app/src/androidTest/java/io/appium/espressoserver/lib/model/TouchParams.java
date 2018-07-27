package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class TouchParams extends AppiumParams {

    @SerializedName("element")
    private String elementId;
    private Long x;
    private Long y;

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
}
