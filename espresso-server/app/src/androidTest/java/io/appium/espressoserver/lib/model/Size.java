package io.appium.espressoserver.lib.model;


import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class Size extends AppiumParams {
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
}
