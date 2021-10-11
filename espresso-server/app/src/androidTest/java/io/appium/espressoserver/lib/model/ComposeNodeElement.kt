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
import androidx.compose.ui.semantics.*
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.model.Rect.Companion.fromBounds

const val COMPOSE_TAG_NAME = "ComposeNode"

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
        get() = node.config.getOrNull(SemanticsProperties.Role)?.toString() ?: COMPOSE_TAG_NAME

    val isPassword: Boolean
        get() = node.config.contains(SemanticsProperties.Password)

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

    fun getAttribute(attributeType: ViewAttributesEnum): String? {
        when (attributeType) {
            ViewAttributesEnum.CONTENT_DESC -> return contentDescription?.toString()
            ViewAttributesEnum.CLASS -> return className
            ViewAttributesEnum.CLICKABLE -> return isClickable.toString()
            ViewAttributesEnum.ENABLED -> return isEnabled.toString()
            ViewAttributesEnum.FOCUSED -> return isFocused.toString()
            ViewAttributesEnum.SCROLLABLE -> return isScrollable.toString()
            ViewAttributesEnum.PASSWORD -> return isPassword.toString()
            ViewAttributesEnum.SELECTED -> return isSelected.toString()
            ViewAttributesEnum.BOUNDS -> return bounds.toShortString()
            ViewAttributesEnum.RESOURCE_ID -> return resourceId
            ViewAttributesEnum.INDEX -> return index.toString()
            ViewAttributesEnum.VIEW_TAG -> return viewTag?.toString()
            ViewAttributesEnum.TEXT -> return text?.toString()
            else -> throw NotYetImplementedException(
                "Only ${ViewAttributesEnum.CONTENT_DESC}, " +
                        "${ViewAttributesEnum.CLASS}, ${ViewAttributesEnum.CLICKABLE}, " +
                        "${ViewAttributesEnum.ENABLED}, ${ViewAttributesEnum.FOCUSED}, " +
                        "${ViewAttributesEnum.SCROLLABLE},${ViewAttributesEnum.PASSWORD}, " +
                        "${ViewAttributesEnum.SELECTED}, ${ViewAttributesEnum.BOUNDS}, " +
                        "${ViewAttributesEnum.RESOURCE_ID}, ${ViewAttributesEnum.INDEX}, " +
                        "${ViewAttributesEnum.VIEW_TAG} and ${ViewAttributesEnum.TEXT} attributes are supported in Compose"
            )
        }
    }
}
