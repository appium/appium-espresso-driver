package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException;

/**
 * Enumerate all possible locator strategies
 */
@SuppressWarnings("unused")
public enum Strategy {
    @SerializedName("class name")
    CLASS_NAME("class name"),
    @SerializedName("css selector")
    CSS_SELECTOR("css selector"),
    @SerializedName("id")
    ID("id"),
    @SerializedName("name")
    NAME("name"),
    @SerializedName("link text")
    LINK_TEXT("link text"),
    @SerializedName("partial link text")
    PARTIAL_LINK_TEXT("partial link text"),
    @SerializedName("xpath")
    XPATH("xpath"),
    @SerializedName("accessibility id")
    ACCESSIBILITY_ID("accessibility id"),
    @SerializedName("text")
    TEXT("text"),

    ANDROID_UIAUTOMATOR("-android uiautomator");

    private final String strategyName;

    public static Strategy fromString(final String text) throws InvalidStrategyException {
        if (text != null) {
            for (final Strategy s : Strategy.values()) {
                if (text.equalsIgnoreCase(s.strategyName)) {
                    return s;
                }
            }
        }
        throw new InvalidStrategyException("Locator strategy '" + text
                + "' is not supported on Android");
    }

    Strategy(final String name) {
        strategyName = name;
    }

    public String getStrategyName() {
        return strategyName;
    }
}