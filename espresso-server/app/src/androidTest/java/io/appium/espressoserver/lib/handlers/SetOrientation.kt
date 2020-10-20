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

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.OrientationParams

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import io.appium.espressoserver.lib.model.OrientationType
import io.appium.espressoserver.lib.viewaction.orientationLandscape
import io.appium.espressoserver.lib.viewaction.orientationPortrait

class SetOrientation : RequestHandler<OrientationParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: OrientationParams): Void? {
        val orientation = params.orientation

        // Validate the orientaiton
        orientation ?: throw AppiumException("Screen orientation value must not be null")

        val supportedValues = OrientationType.values().map { it.name }
        if (!supportedValues.contains(orientation.toUpperCase())) {
            throw AppiumException("Screen orientation must be one of $supportedValues. Found '$orientation'")
        }

        // Get the view interaction for the element or for the root, if no element provided
        val viewInteraction = params.elementId?.let {elementId ->
            Element.getViewInteractionById(elementId)
        } ?: run {
            onView(isRoot())
        }

        try {
            if (orientation.equals(OrientationType.LANDSCAPE.name, ignoreCase = true)) {
                viewInteraction.perform(orientationLandscape())
            } else {
                viewInteraction.perform(orientationPortrait())
            }
            return null
        } catch (e: Exception) {
            throw AppiumException("Cannot change screen orientation to '$orientation'", e)
        }
    }
}
