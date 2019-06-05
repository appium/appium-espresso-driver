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

import androidx.test.espresso.EspressoException
import androidx.test.espresso.contrib.PickerActions
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.SetTimeParams

class SetTime : RequestHandler<SetTimeParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: SetTimeParams): Void? {
        val viewInteraction = Element.getViewInteractionById(params.elementId)
        try {
            viewInteraction.perform(PickerActions.setTime(params.hours, params.minutes))
        } catch (e: Exception) {
            if (e is EspressoException) {
                throw AppiumException("Could not set time on element. Reason: ${e}")
            }
            throw e
        }

        return null
    }
}