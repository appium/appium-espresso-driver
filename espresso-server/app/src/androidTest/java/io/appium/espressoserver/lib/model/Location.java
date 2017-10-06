package io.appium.espressoserver.lib.model;


import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class Location extends AppiumParams {
    private Integer x = null;
    private Integer y = null;

    @Nullable
    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Nullable
    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

}
