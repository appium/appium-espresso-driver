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
import androidx.test.espresso.contrib.DrawerActions
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.DrawerActionParams
import io.appium.espressoserver.lib.model.Element

class DrawerActionHandler(private val isOpenAction: Boolean) : RequestHandler<DrawerActionParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: DrawerActionParams): Void? {
        val viewInteraction = Element.getViewInteractionById(params.elementId)
        try {
            params.gravity?.let {gravity ->
                viewInteraction.perform(if (isOpenAction) DrawerActions.open(gravity) else DrawerActions.close(gravity))
            } ?: run {
                viewInteraction.perform(if (isOpenAction) DrawerActions.open() else DrawerActions.close())
            }
        } catch (e: Exception) {
            if (e is EspressoException) {
                throw AppiumException(String.format("Could not %s drawer. Reason: %s", if (isOpenAction) "open" else "close", e))
            }
            throw e
        }

        return null
    }
}
