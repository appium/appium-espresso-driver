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
import androidx.compose.ui.test.*
import io.appium.espressoserver.EspressoServerRunnerTest
import java.lang.AssertionError

class ComposeNodeElement(private val node: SemanticsNode) {

    val contentDescription: CharSequence?
        get() = node.config.getOrNull(SemanticsProperties.ContentDescription)?.get(0)

    val viewTag: CharSequence?
        get() = node.config.getOrNull(SemanticsProperties.TestTag)

    val bounds: Rect
        get() {
            val bounds = node.boundsInRoot
            return Rect(
                bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(),
                bounds.bottom.toInt()
            )
        }

    val resourceId: String
        get() = node.id.toString()

    val text: String
        get() = node.config.getOrNull(SemanticsProperties.Text)?.get(0).toString()

    val index: Int
        get() {
            val parent = node.parent
            parent?.let {
                it.children.mapIndexed() { index, childNode -> if (node.id == childNode.id) return index }
            }
            return 0
        }

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

    val isVisible: Boolean
        get() {
            return try {
                getSemanticsNodeInteraction().assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

    private fun getSemanticsNodeInteraction() =
        EspressoServerRunnerTest.composeTestRule.onNode(hasId(node.id))

    private fun hasId(id: Int): SemanticsMatcher {
        return SemanticsMatcher(
            "hasNodeId"
        ) {
            it.id == id
        }
    }

}
