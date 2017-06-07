package io.appium.espressoserver.lib.model;

public enum ViewAttributesEnum {

    CONTENT_DESC("content-desc"),
    CLASS("class"),
    TEXT("text"),
    PACKAGE("package"),
    CHECKABLE("checkable"),
    CHECKED("checked"),
    CLICKABLE("clickable"),
    ENABLED("enabled"),
    FOCUSABLE("focusable"),
    FOCUSED("focused"),
    SCROLLABLE("scrollable"),
    LONG_CLICKABLE("long-clickable"),
    PASSWORD("password"),
    SELECTED("selected"),
    BOUNDS("bounds"),
    RESOURCE_ID("resource-id"),
    INSTANCE("instance"),
    INDEX("index");


    private final String name;

    ViewAttributesEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
