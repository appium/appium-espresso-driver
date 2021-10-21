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

enum class ViewAttributesEnum {
    //  Common Attributes
    BOUNDS,
    CLASS,
    CLICKABLE,
    CONTENT_DESC,
    ENABLED,
    FOCUSED,
    INDEX,
    PASSWORD,
    RESOURCE_ID,
    SCROLLABLE,
    SELECTED,
    TEXT,
    VIEW_TAG,

    //  Driver specific attributes
    ADAPTERS,
    ADAPTER_TYPE,
    CHECKABLE,
    CHECKED,
    FOCUSABLE,
    HINT,
    INSTANCE,
    LONG_CLICKABLE,
    NO_ELLIPSIZED_TEXT,
    NO_MULTILINE_BUTTONS,
    NO_OVERLAPS,
    PACKAGE,
    VISIBLE;

    override fun toString(): String {
        return this.name.replace("_", "-").lowercase()
    }
}

class EspressoAttributes(override var attributes: List<ViewAttributesEnum> =
                             commonAttributes + espressoAttributes) : CommonAttributes() {
    companion object {
        private val espressoAttributes: List<ViewAttributesEnum> by lazy {
            listOf(
                ViewAttributesEnum.ADAPTERS,
                ViewAttributesEnum.ADAPTER_TYPE,
                ViewAttributesEnum.CHECKABLE,
                ViewAttributesEnum.CHECKED,
                ViewAttributesEnum.FOCUSABLE,
                ViewAttributesEnum.HINT,
                ViewAttributesEnum.INSTANCE,
                ViewAttributesEnum.LONG_CLICKABLE,
                ViewAttributesEnum.NO_ELLIPSIZED_TEXT,
                ViewAttributesEnum.NO_MULTILINE_BUTTONS,
                ViewAttributesEnum.NO_OVERLAPS,
                ViewAttributesEnum.PACKAGE,
                ViewAttributesEnum.VISIBLE
            )
        }
    }
}

class ComposeAttributes(override var attributes: List<ViewAttributesEnum> = commonAttributes) : CommonAttributes()

abstract class CommonAttributes() {
    companion object {
        val commonAttributes: List<ViewAttributesEnum> by lazy {
            listOf(
                ViewAttributesEnum.BOUNDS,
                ViewAttributesEnum.CLASS,
                ViewAttributesEnum.CLICKABLE,
                ViewAttributesEnum.CONTENT_DESC,
                ViewAttributesEnum.ENABLED,
                ViewAttributesEnum.FOCUSED,
                ViewAttributesEnum.INDEX,
                ViewAttributesEnum.PASSWORD,
                ViewAttributesEnum.RESOURCE_ID,
                ViewAttributesEnum.SCROLLABLE,
                ViewAttributesEnum.SELECTED,
                ViewAttributesEnum.TEXT,
                ViewAttributesEnum.VIEW_TAG,
            )
        }
    }
    abstract var attributes: List<ViewAttributesEnum>

    fun contains(value: ViewAttributesEnum): Boolean {
        return attributes.contains(value)
    }

    fun supportedAttributes(): String {
        return attributes.joinToString{ "\'${it}\'" }
    }

    fun valueOf(value: String): ViewAttributesEnum? {
        return attributes.find { it.name == value }
    }
}
