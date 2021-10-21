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

package io.appium.espressoserver.lib.helpers

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.*
import io.appium.espressoserver.EspressoServerRunnerTest
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.model.Strategy
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.model.Locator
import io.appium.espressoserver.lib.model.SourceDocument
import io.appium.espressoserver.lib.model.AttributesEnum
import io.appium.espressoserver.lib.viewmatcher.fetchIncludedAttributes

/**
 * Retrieve cached node and return the SemanticsNodeInteraction
 */
fun getNodeInteractionById(elementId: String?): SemanticsNodeInteraction =
    elementId?.let { ComposeViewCache.get(it) ?: throw StaleElementException(it) }
        ?: throw InvalidArgumentException("Cannot find 'null' element")

// https://developer.android.com/jetpack/compose/semantics#merged-vs-unmerged
fun toNodeInteractionsCollection(params: Locator): SemanticsNodeInteractionCollection {
    val parentNodeInteraction = params.elementId?.let { getNodeInteractionById(it) }
    return parentNodeInteraction?.findDescendantNodeInteractions(params)
        ?: EspressoServerRunnerTest.composeTestRule.onAllNodes(
            useUnmergedTree = true,
            matcher = semanticsMatcherForLocator(params)
        )
}

fun SemanticsNodeInteraction.findDescendantNodeInteractions(locator: Locator): SemanticsNodeInteractionCollection =
    this.onChildren().filter(semanticsMatcherForLocator(locator))

fun semanticsMatcherForLocator(locator: Locator): SemanticsMatcher =
    when (locator.using) {
        Strategy.VIEW_TAG -> hasTestTag(locator.value!!)
        Strategy.TEXT -> hasText(locator.value!!)
        Strategy.LINK_TEXT -> hasText(locator.value!!)
        Strategy.ACCESSIBILITY_ID -> hasContentDescription(locator.value!!)
        Strategy.XPATH -> hasXpath(locator)
        else -> throw InvalidSelectorException(
            "Can't use non-Compose selectors. " +
                    "Only ${Strategy.VIEW_TAG}, ${Strategy.TEXT}, ${Strategy.LINK_TEXT}, ${Strategy.XPATH} and " +
                    "${Strategy.ACCESSIBILITY_ID} are supported"
        )
    }

private fun hasXpath(locator: Locator): SemanticsMatcher {
    val matchingIds = SourceDocument(
        locator.elementId?.let { getSemanticsNode(it) }, fetchIncludedAttributes(locator.value!!)
    ).matchingNodeIds(locator.value!!, AttributesEnum.RESOURCE_ID.toString())

    return SemanticsMatcher("Matches Xpath ${locator.value}") {
        matchingIds.contains(it.id)
    }
}

fun getSemanticsNode(elementId: String): SemanticsNode =
    try {
        getNodeInteractionById(elementId).fetchSemanticsNode()
    } catch (e: AssertionError) {
        throw StaleElementException(elementId)
    }
