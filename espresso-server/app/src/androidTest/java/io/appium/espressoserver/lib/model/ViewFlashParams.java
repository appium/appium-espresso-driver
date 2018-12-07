package io.appium.espressoserver.lib.model;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class ViewFlashParams extends AppiumParams {

    private Integer durationMillis;

    private Integer repeatCount;

    @Nullable
    public Integer getDurationMillis() {
        return durationMillis;
    }

    @Nullable
    public Integer getRepeatCount() {
        return repeatCount;
    }
}
