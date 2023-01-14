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

enum class AttributesEnum {
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

class EspressoAttributes : CommonAttributes(commonAttributes + espressoAttributes) {
    companion object {
        private val espressoAttributes: List<AttributesEnum> by lazy {
            listOf(
                AttributesEnum.ADAPTERS,
                AttributesEnum.ADAPTER_TYPE,
                AttributesEnum.CHECKABLE,
                AttributesEnum.FOCUSABLE,
                AttributesEnum.HINT,
                AttributesEnum.INSTANCE,
                AttributesEnum.LONG_CLICKABLE,
                AttributesEnum.NO_ELLIPSIZED_TEXT,
                AttributesEnum.NO_MULTILINE_BUTTONS,
                AttributesEnum.NO_OVERLAPS,
                AttributesEnum.PACKAGE,
                AttributesEnum.VISIBLE
            )
        }
    }
}

class ComposeAttributes : CommonAttributes(commonAttributes)

abstract class CommonAttributes(var attributes: List<AttributesEnum>) {
    companion object {
        val commonAttributes: List<AttributesEnum> by lazy {
            listOf(
                AttributesEnum.BOUNDS,
                AttributesEnum.CHECKED,
                AttributesEnum.CLASS,
                AttributesEnum.CLICKABLE,
                AttributesEnum.CONTENT_DESC,
                AttributesEnum.ENABLED,
                AttributesEnum.FOCUSED,
                AttributesEnum.INDEX,
                AttributesEnum.PASSWORD,
                AttributesEnum.RESOURCE_ID,
                AttributesEnum.SCROLLABLE,
                AttributesEnum.SELECTED,
                AttributesEnum.TEXT,
                AttributesEnum.VIEW_TAG,
            )
        }
    }

    fun contains(value: AttributesEnum): Boolean {
        return attributes.contains(value)
    }

    fun supportedAttributes(): String {
        return attributes.joinToString{ "\'${it}\'" }
    }

    fun valueOf(value: String): AttributesEnum? {
        return attributes.find { it.toString().equals(value, ignoreCase = false) }
    }
}
