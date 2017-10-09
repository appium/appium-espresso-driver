package io.appium.espressoserver.lib.model;


import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class Rect extends AppiumParams {
    private Integer x = null;
    private Integer y = null;
    private Integer width = null;
    private Integer height = null;

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Nullable
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

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
