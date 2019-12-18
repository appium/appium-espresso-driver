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

/**
 * Enumerate all possible locator strategies
 */
enum class Strategy(val strategyName: String) {
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
    @SerializedName(value = "-android viewtag", alternate = ["tag name"])
    VIEW_TAG("-android viewtag"),
    @SerializedName(value = "-android datamatcher")
    DATAMATCHER("-android datamatcher"),
    @SerializedName(value = "-android viewmatcher")
    VIEWMATCHER("-android viewmatcher");
}