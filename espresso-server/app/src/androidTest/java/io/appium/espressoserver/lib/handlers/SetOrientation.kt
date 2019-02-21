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

import java.util.Arrays

import androidx.test.espresso.ViewInteraction
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.OrientationParams
import io.appium.espressoserver.lib.viewaction.OrientationChange

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot

class SetOrientation : RequestHandler<OrientationParams, Void?> {

    @Throws(AppiumException::class)
    override fun handle(params: OrientationParams): Void? {
        val orientation = params.orientation
        orientation ?: throw AppiumException("Screen orientation value must not be null")

        // Validate the orientaiton
        if (!listOf("LANDSCAPE", "PORTRAIT").contains(orientation.toUpperCase())) {
            throw AppiumException("Screen orientation must be one of LANDSCAPE or PORTRAIT. Found '${orientation}'");
        }

        // Get the view interaction for the element or for the root, if no element provided
        val viewInteraction = params.elementId?.let {elementId ->
            Element.getViewInteractionById(elementId)
        } ?: run {
            onView(isRoot())
        }

        try {
            if (orientation.equals("LANDSCAPE", ignoreCase = true)) {
                viewInteraction.perform(OrientationChange.orientationLandscape())
            } else {
                viewInteraction.perform(OrientationChange.orientationPortrait())
            }
            return null;
        } catch (e: Exception) {
            throw AppiumException("Cannot change screen orientation to '${orientation}'", e)
        }
    }
}
