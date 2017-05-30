package io.appium.espressoserver.lib.Model;

import io.appium.espressoserver.lib.Exceptions.InvalidStrategyException;

/**
 * Enumerate all possible locator strategies
 */
@SuppressWarnings("unused")
public enum Strategy {
    CLASS_NAME("class name"),
    CSS_SELECTOR("css selector"),
    ID("id"),
    NAME("name"),
    LINK_TEXT("link text"),
    PARTIAL_LINK_TEXT("partial link text"),
    XPATH("xpath"),
    ACCESSIBILITY_ID("accessibility id"),
    TEXT("text"),
    ANDROID_UIAUTOMATOR("-android uiautomator");

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

    private final String strategyName;

    Strategy(final String name) {
        strategyName = name;
    }

    public String getStrategyName() {
        return strategyName;
    }
}