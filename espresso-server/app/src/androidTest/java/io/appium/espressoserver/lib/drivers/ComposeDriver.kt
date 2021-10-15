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

package io.appium.espressoserver.lib.drivers

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import io.appium.espressoserver.EspressoServerRunnerTest
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.helpers.*
import io.appium.espressoserver.lib.model.*
import io.appium.espressoserver.lib.model.Rect

class ComposeDriver : AppDriver {
    override val name = DriverContext.StrategyType.COMPOSE

    override fun findElement(params: Locator): BaseElement {
        val nodeInteractions = toNodeInteractionsCollection(params)
        if (nodeInteractions.fetchSemanticsNodes(false).isEmpty()) throw NoSuchElementException(
            String.format(
                "Could not find a compose element with strategy '%s' and selector '%s'",
                params.using, params.value
            )
        )
        return ComposeElement(nodeInteractions[0])
    }

    override fun findElements(params: Locator): List<BaseElement> {
        val nodeInteractions = toNodeInteractionsCollection(params)
        return nodeInteractions.fetchSemanticsNodes(false)
            .mapIndexed { index, _ -> ComposeElement(nodeInteractions[index]) }
    }

    // https://developer.android.com/jetpack/compose/semantics#merged-vs-unmerged
    private fun toNodeInteractionsCollection(params: Locator): SemanticsNodeInteractionCollection {
        val parentNodeInteraction = params.elementId?.let { getNodeInteractionById(it) }
        return parentNodeInteraction?.findDescendantNodeInteractions(params)
            ?: EspressoServerRunnerTest.composeTestRule.onAllNodes(
                useUnmergedTree = true,
                matcher = semanticsMatcherForLocator(params)
            )
    }

    override fun getAttribute(elementId: String, attributeType: ViewAttributesEnum): String? =
        ComposeNodeElement(getSemanticsNode(elementId)).getAttribute(attributeType)
}