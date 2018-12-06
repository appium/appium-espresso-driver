package io.appium.espressoserver.lib.model;

public class ViewFlashParams extends AppiumParams {

    @SuppressWarnings("unused")
    private int durationMillis;

    @SuppressWarnings("unused")
    private int repeatCount;

    public int getDurationMillis() {
        return durationMillis;
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
