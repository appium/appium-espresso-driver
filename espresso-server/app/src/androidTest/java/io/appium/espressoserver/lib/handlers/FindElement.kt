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

package io.appium.espressoserver.lib.handlers

import android.view.View

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidStrategyException
import io.appium.espressoserver.lib.handlers.exceptions.MissingCommandsException
import io.appium.espressoserver.lib.handlers.exceptions.NoSuchElementException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.Locator
import io.appium.espressoserver.lib.viewaction.ViewGetter

import io.appium.espressoserver.lib.helpers.ViewFinder.findBy

class FindElement : RequestHandler<Locator, Element> {

    @Throws(AppiumException::class)
    override fun handle(locator: Locator): Element {
        var parentView: View? = null
        locator.elementId?.let {
            parentView = ViewGetter().getView(Element.getViewInteractionById(it))
        }
        if (locator.using == null) {
            throw InvalidStrategyException("Locator strategy cannot be empty")
        } else if (locator.value == null) {
            throw MissingCommandsException("No locator provided")
        }
        // Test the selector
        val view = findBy(parentView, locator.using, locator.value) ?: throw NoSuchElementException(
                String.format("Could not find element with strategy %s and selector %s",
                        locator.using, locator.value))

        // If we have a match, return success
        return Element(view)
    }
}
