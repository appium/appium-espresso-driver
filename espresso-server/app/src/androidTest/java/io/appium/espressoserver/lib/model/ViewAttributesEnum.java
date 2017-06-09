package io.appium.espressoserver.lib.model;

public enum ViewAttributesEnum {

    CONTENT_DESC,
    CLASS,
    TEXT,
    PACKAGE,
    CHECKABLE,
    CHECKED,
    CLICKABLE,
    ENABLED,
    FOCUSABLE,
    FOCUSED,
    SCROLLABLE,
    LONG_CLICKABLE,
    PASSWORD,
    SELECTED,
    BOUNDS,
    RESOURCE_ID,
    INSTANCE,
    INDEX;


    @Override
    public String toString() {
        return this.name().replace("_", "-").toLowerCase();
    }
}
