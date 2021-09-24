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

class ComposeNodeElement(private val node: SemanticsNode) {

    val contentDescription: CharSequence? =
        node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()

    val text: CharSequence? = node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()

    val resourceId: String = node.id.toString()

    val viewTag: CharSequence? = node.config.getOrNull(SemanticsProperties.TestTag)

    val isClickable: Boolean = node.config.contains(SemanticsActions.OnClick)

    val isEnabled: Boolean = !node.config.contains(SemanticsProperties.Disabled)

    val isFocused: Boolean = node.config.getOrNull(SemanticsProperties.Focused) == true

    val isScrollable: Boolean = node.config.contains(SemanticsActions.ScrollBy)

    val isSelected: Boolean = node.config.getOrNull(SemanticsProperties.Selected) == true

    val className: String =
        node.config.getOrNull(SemanticsProperties.Role)?.toString() ?: "ComposeNode"

    val progress: String? =
        node.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)?.toString()

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

    val isPassword: Boolean? = node.config.getOrNull(SemanticsProperties.Password)?.let { true }
}
