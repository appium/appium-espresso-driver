package io.appium.espressoserver.lib.model;

import android.support.annotation.Nullable;

public class ViewText {
    private boolean isHint = false;
    private String rawText;

    public ViewText(@Nullable String rawText, boolean isHint) {
        this.rawText = rawText;
        this.isHint = isHint;
    }

    public ViewText(int rawText) {
        this(Integer.toString(rawText), false);
    }

    public String getText() {
        return rawText == null ? "" : rawText;
    }

    @Nullable
    public String getRawText() {
        return rawText;
    }

    public boolean isHint() {
        return isHint;
    }

    @Override
    public String toString() {
        return getText();
    }
}
