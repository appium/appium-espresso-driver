/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.model

import com.google.gson.annotations.SerializedName

class UiautomatorParams : AppiumParams() {
    val strategy: Strategy? = null
    val locator: String? = null
    val index: Int? = null
    val action: Action? = null

    enum class Strategy constructor(val strategyName: String) {
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
        APPLICATION_PACKAGE("pkg");
    }

    enum class Action constructor(val actionName: String) {
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
        IS_SELECTED("isSelected");
    }
}
