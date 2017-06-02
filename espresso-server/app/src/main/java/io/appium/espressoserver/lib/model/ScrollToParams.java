package io.appium.espressoserver.lib.model;

@SuppressWarnings("unused")
public class ScrollToParams extends AppiumParams {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
