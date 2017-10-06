package io.appium.espressoserver.lib.model;


import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class OrientationParams extends AppiumParams {
    private String orientation = null;

    @Nullable
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}
