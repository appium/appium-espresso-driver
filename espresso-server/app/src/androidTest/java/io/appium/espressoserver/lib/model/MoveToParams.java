package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MoveToParams extends AppiumParams {
    @SerializedName("element")
    private String elementId;
    private int xoffset;
    private int yoffset;

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Nullable
    public int getXOffset() {
        return xoffset;
    }

    @Nullable
    public int getYOffset() {
        return yoffset;
    }
}
