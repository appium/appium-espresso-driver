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

import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException
import io.appium.espressoserver.lib.helpers.ViewFinder
import io.appium.espressoserver.lib.model.*
import io.appium.espressoserver.lib.viewaction.ViewGetter
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class EspressoDriver : AppDriver {
    override val name = DriverContext.StrategyType.ESPRESSO

    override fun findElement(params: Locator): BaseElement {
        val parentView = params.elementId?.let {
            ViewGetter().getView(EspressoElement.getViewInteractionById(it))
        }
        // Test the selector
        val view = ViewFinder.findBy(
            parentView,
            params.using ?: throw InvalidSelectorException("Locator strategy cannot be empty"),
            params.value ?: throw InvalidArgumentException()
        )
            ?: throw NoSuchElementException(
                String.format(
                    "Could not find espresso element with strategy %s and selector %s",
                    params.using, params.value
                )
            )

        // If we have a match, return success
        return EspressoElement(view)
    }

    override fun findElements(params: Locator): List<EspressoElement> {
        val parentView = params.elementId?.let {
            ViewGetter().getView(EspressoElement.getViewInteractionById(it))
        }

        // Return as list of Elements
        return ViewFinder.findAllBy(
            parentView,
            params.using ?: throw InvalidSelectorException("Locator strategy cannot be empty"),
            params.value ?: throw InvalidArgumentException()
        )
            .map { EspressoElement(it) }
    }

    override fun click(params: AppiumParams): Unit {
        try {
            EspressoElement.getViewInteractionById(params.elementId).perform(ViewActions.click())
        } catch (e: PerformException) {
            throw InvalidElementStateException("click", params.elementId!!, e)
        }
    }

    override fun getText(params: AppiumParams): String {
        val viewInteraction = EspressoElement.getViewInteractionById(params.elementId)
        return ViewTextGetter()[viewInteraction].rawText
    }

    override fun getDisplayed(params: AppiumParams): Boolean =
        ViewElement(EspressoElement.getViewById(params.elementId, false)).isVisible

}