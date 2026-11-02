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

import android.graphics.Point
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.compose.ComposeHandlerBridge
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class PerformAction : RequestHandler<Actions, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: Actions): Void? {
        val elementCenters = resolveComposeElementCenters(params)

        val runnable = object: UiControllerRunnable<Void?> {
            override fun run(uiController: UiController): Void? {
                val adapter = EspressoW3CActionAdapter(uiController, elementCenters)
                params.adapter = adapter
                try {
                    params.perform(params.sessionId!!)
                } catch (e: AppiumException) {
                    // The next request has no record of injected keys, so release them now
                    adapter.releasePressedKeys()
                    throw e
                }
                return null
            }
        }

        UiControllerPerformer(runnable).run()
        return null
    }

    // Compose elements cannot be resolved on the main thread, so resolve them up front
    private fun resolveComposeElementCenters(params: Actions): Map<String, Point> {
        if (DriverContext.currentStrategyType != DriverContext.StrategyType.COMPOSE) {
            return emptyMap()
        }
        return params.actions.orEmpty()
            .flatMap { it.actions.orEmpty() }
            .filter { it.isOriginElement }
            .mapNotNull { it.origin.elementId }
            .distinct()
            .associateWith { ComposeHandlerBridge.getElementCenterPoint(it) }
    }
}
