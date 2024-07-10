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

import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder
import io.appium.espressoserver.lib.model.TouchActionsParams
import io.appium.espressoserver.lib.model.toW3CInputSources
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class TouchAction : RequestHandler<TouchActionsParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: TouchActionsParams): Void? {
        val runnable = object : UiControllerRunnable<Void?> {
            override fun run(uiController: UiController): Void? {
                val inputSources = toW3CInputSources(listOf(params.actions))
                val actions = ActionsBuilder()
                        .withAdapter(EspressoW3CActionAdapter(uiController))
                        .withActions(inputSources)
                        .build()
                actions.perform(params.sessionId!!)
                actions.release(params.sessionId!!)

                return null
            }
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
