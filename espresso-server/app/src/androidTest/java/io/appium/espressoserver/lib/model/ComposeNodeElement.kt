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

import android.graphics.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.model.Rect.Companion.fromBounds

const val DEFAULT_TAG_NAME = "ComposeNode"
val composeAttributes by lazy { ComposeAttributes() }

val POSSIBLE_CLASS_PROPERTIES: List<SemanticsPropertyKey<out Any>> by lazy {
    listOf(
        SemanticsProperties.EditableText,
        SemanticsProperties.Text,
        SemanticsProperties.ProgressBarRangeInfo,
        SemanticsProperties.HorizontalScrollAxisRange,
        SemanticsProperties.VerticalScrollAxisRange,
        SemanticsProperties.SelectableGroup,
        SemanticsProperties.PaneTitle,
        SemanticsProperties.Password
    )
}

class ComposeNodeElement(private val node: SemanticsNode) {

    val contentDescription: CharSequence?
        get() = node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()

    val text: String?
        get() = node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.toString()
            ?: node.config.getOrNull(SemanticsProperties.EditableText)?.text
            ?: node.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)?.current?.toString()

    val resourceId: String
        get() = node.id.toString()

    val viewTag: CharSequence?
        get() = node.config.getOrNull(SemanticsProperties.TestTag)

    val isClickable: Boolean
        get() = node.config.contains(SemanticsActions.OnClick)

    val isEnabled: Boolean
        get() = !node.config.contains(SemanticsProperties.Disabled)

    val isFocused: Boolean
        get() = node.config.getOrNull(SemanticsProperties.Focused) == true

    val isScrollable: Boolean
        get() = node.config.contains(SemanticsActions.ScrollBy)

    val isSelected: Boolean
        get() = node.config.getOrNull(SemanticsProperties.Selected) == true

    val className: String
        get() {
            //  Compose API doesn't have class name info, as a workaround relaying on node SemanticsProperties.
            return node.config.getOrNull(SemanticsProperties.Role)?.toString()
                ?: POSSIBLE_CLASS_PROPERTIES.firstOrNull { node.config.contains(it) }?.name
                ?: DEFAULT_TAG_NAME
        }

    val isPassword: Boolean
        get() = node.config.contains(SemanticsProperties.Password)

    val isChecked: Boolean
        get() = node.config.getOrNull(SemanticsProperties.ToggleableState) == ToggleableState.On

    val index: Int
        get() {
            node.parent?.let {
                it.children.mapIndexed { index, childNode -> if (node.id == childNode.id) return index }
            }
            return 0
        }

    val bounds: Rect
        get() {
            val bounds = node.boundsInWindow
            return Rect(
                bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(),
                bounds.bottom.toInt()
            )
        }

    val rect: io.appium.espressoserver.lib.model.Rect
        get() = fromBounds(bounds)

    fun getAttribute(attributeName: String): String? {
        when (composeAttributes.valueOf(attributeName)) {
            AttributesEnum.CONTENT_DESC -> return contentDescription?.toString()
            AttributesEnum.CLASS -> return className
            AttributesEnum.CLICKABLE -> return isClickable.toString()
            AttributesEnum.ENABLED -> return isEnabled.toString()
            AttributesEnum.FOCUSED -> return isFocused.toString()
            AttributesEnum.SCROLLABLE -> return isScrollable.toString()
            AttributesEnum.PASSWORD -> return isPassword.toString()
            AttributesEnum.SELECTED -> return isSelected.toString()
            AttributesEnum.BOUNDS -> return bounds.toShortString()
            AttributesEnum.RESOURCE_ID -> return resourceId
            AttributesEnum.INDEX -> return index.toString()
            AttributesEnum.VIEW_TAG -> return viewTag?.toString()
            AttributesEnum.TEXT -> return text?.toString()
            AttributesEnum.CHECKED -> return isChecked.toString()
            else -> throw NotYetImplementedException(
                "Compose doesn't support attribute '$attributeName', Attribute name should be one of ${composeAttributes.supportedAttributes()}")
        }
    }
}
