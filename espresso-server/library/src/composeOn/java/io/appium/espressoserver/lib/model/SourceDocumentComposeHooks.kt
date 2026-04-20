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

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SelectionResult
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onRoot
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.drivers.composeTestRule
import io.appium.espressoserver.lib.helpers.AndroidLogger
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

internal object SourceDocumentComposeHooks {
    fun appendComposeStrategy(doc: SourceDocument, root: Any?) {
        if (root != null) {
            serializeComposeNode(doc, root as SemanticsNode, 0)
        } else {
            val rootNodes = rootSemanticNodes()
            if (rootNodes.size == 1) {
                serializeComposeNode(doc, rootNodes.first(), 0)
            } else {
                doc.serializer?.startTag(NAMESPACE, DEFAULT_TAG_NAME)
                rootNodes.forEach { semanticsNode -> serializeComposeNode(doc, semanticsNode, 0) }
                doc.serializer?.endTag(NAMESPACE, DEFAULT_TAG_NAME)
            }
        }
    }

    private fun serializeComposeNode(doc: SourceDocument, semanticsNode: SemanticsNode?, depth: Int) {
        if (semanticsNode == null) {
            return
        }
        val nodeElement = ComposeNodeElement(semanticsNode)
        val className = nodeElement.className
        val tagName = toXmlNodeName(className)
        doc.serializer?.startTag(NAMESPACE, tagName)

        linkedMapOf(
            AttributesEnum.CLASS to { className },
            AttributesEnum.INDEX to { nodeElement.index },
            AttributesEnum.CLICKABLE to { nodeElement.isClickable },
            AttributesEnum.ENABLED to { nodeElement.isEnabled },
            AttributesEnum.FOCUSED to { nodeElement.isFocused },
            AttributesEnum.SCROLLABLE to { nodeElement.isScrollable },
            AttributesEnum.SELECTED to { nodeElement.isSelected },
            AttributesEnum.CHECKED to { nodeElement.isChecked },
            AttributesEnum.VIEW_TAG to { nodeElement.viewTag },
            AttributesEnum.CONTENT_DESC to { nodeElement.contentDescription },
            AttributesEnum.BOUNDS to { nodeElement.bounds.toShortString() },
            AttributesEnum.TEXT to { nodeElement.text },
            AttributesEnum.PASSWORD to { nodeElement.isPassword },
            AttributesEnum.RESOURCE_ID to { nodeElement.resourceId },
        ).forEach {
            doc.setAttribute(it.key, it.value())
        }

        if (depth < MAX_TRAVERSAL_DEPTH) {
            val children = semanticsNode.children
            for (index in 0 until children.count()) {
                serializeComposeNode(doc, children[index], depth + 1)
            }
        } else {
            AndroidLogger.warn(
                "Skipping traversal of ${semanticsNode.javaClass.name}'s children, since " +
                    "the current depth has reached its maximum allowed value of $depth",
            )
        }

        doc.serializer?.endTag(NAMESPACE, tagName)
    }

    private fun rootSemanticNodes(): List<SemanticsNode> =
        try {
            listOf(DriverContext.composeTestRule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        } catch (e: AssertionError) {
            val result: SelectionResult =
                SemanticsNodeInteraction::class.declaredMemberFunctions.find { it.name == "fetchSemanticsNodes" }?.let {
                    it.isAccessible = true
                    if (it.parameters.size == 4) {
                        it.call(DriverContext.composeTestRule.onRoot(useUnmergedTree = true), true, null, true)
                    } else {
                        it.call(DriverContext.composeTestRule.onRoot(useUnmergedTree = true), true, null)
                    }
                } as SelectionResult
            result.selectedNodes
        }
}
