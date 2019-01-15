package io.appium.espressoserver.lib.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UiautomatorParams extends AppiumParams {
    private Strategy strategy;
    private String value;
    private Integer index;
    private Action action;

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    @Nullable
    public Integer getIndex() {
        return index;
    }

    public enum Strategy {
        @SerializedName("clazz")
        CLASS_NAME("clazz"),

        @SerializedName("res")
        ID("res"),

        @SerializedName("text")
        TEXT("text"),

        @SerializedName("textContains")
        TEXT_CONTAIN("textContains"),

        @SerializedName("textEndsWith")
        TEXT_ENDS_WITH("textEndsWith"),

        @SerializedName("textStartsWith")
        TEXT_STARTS_WITH("textStartsWith"),

        @SerializedName("desc")
        DESC("desc"),

        @SerializedName("descContains")
        DESC_CONTAINS("descContains"),

        @SerializedName("descEndsWith")
        DESC_ENDS_WITH("descEndsWith"),

        @SerializedName("descStartsWith")
        DESC_STARTS_WITH("descStartsWith"),

        @SerializedName("pkg")
        APPLICATION_PACKAGE("pkg"),;

        private final String name;

        Strategy(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static List<String> getValidStrategyNames() {
            List<String> validStrategies = new ArrayList<>();
            for (Strategy strategy : Strategy.values()) {
                validStrategies.add(strategy.getName());
            }
            return validStrategies;
        }
    }


    public enum Action {
        @SerializedName("click")
        CLICK("click"),

        @SerializedName("longClick")
        LONG_CLICK("longClick"),

        @SerializedName("getText")
        GET_TEXT("getText"),

        @SerializedName("getContentDescription")
        GET_CONTENT_DESCRIPTION("getContentDescription"),

        @SerializedName("getClassName")
        GET_CLASS_NAME("getClassName"),

        @SerializedName("getResourceName")
        GET_RESOURCE_NAME("getResourceName"),

        @SerializedName("getVisibleBounds")
        GET_VISIBLE_BOUNDS("getVisibleBounds"),

        @SerializedName("getVisibleCenter")
        GET_VISIBLE_CENTER("getVisibleCenter"),

        @SerializedName("getApplicationPackage")
        GET_APPLICATION_PACKAGE("getApplicationPackage"),

        @SerializedName("getChildCount")
        GET_CHILD_COUNT("getChildCount"),

        @SerializedName("clear")
        CLEAR("clear"),

        @SerializedName("isCheckable")
        IS_CHECKABLE("isCheckable"),

        @SerializedName("isChecked")
        IS_CHECKED("isChecked"),

        @SerializedName("isClickable")
        IS_CLICKABLE("isClickable"),

        @SerializedName("isEnabled")
        IS_ENABLED("isEnabled"),

        @SerializedName("isFocusable")
        IS_FOCUSABLE("isFocusable"),

        @SerializedName("isFocused")
        IS_FOCUSED("isFocused"),

        @SerializedName("isLongClickable")
        IS_LONG_CLICKABLE("isLongClickable"),

        @SerializedName("isScrollable")
        IS_SCROLLABLE("isScrollable"),

        @SerializedName("isSelected")
        IS_SELECTED("isSelected"),;

        private final String name;

        Action(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static List<String> getValidActionNames() {
            List<String> validStrategies = new ArrayList<>();
            for (Action action : Action.values()) {
                validStrategies.add(action.getName());
            }
            return validStrategies;
        }
    }
}
