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

import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.helpers.getNodeInteractionById
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionBuilder
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceBuilder
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY
import io.appium.espressoserver.lib.model.TextValueParams

class Keys : RequestHandler<TextValueParams, Unit> {

    override fun handleEspresso(params: TextValueParams): Unit {
        val keys = params.value ?: emptyList()

        val runnable = object : UiControllerRunnable<Void?> {
            override fun run(uiController: UiController): Void? {
                // Add a list of keyDown + keyUp actions for each key
                val keyActions = arrayListOf<Action>()
                keys.forEach {
                    // Key down event
                    keyActions.add(ActionBuilder()
                            .withType(KEY_DOWN)
                            .withValue(it)
                            .build()
                    )

                    // Key up event
                    keyActions.add(ActionBuilder()
                            .withType(KEY_UP)
                            .withValue(it)
                            .build()
                    )
                }

                val keyInputSource = InputSourceBuilder()
                        .withId("keyboard")
                        .withType(KEY)
                        .withActions(keyActions)
                        .build()

                val actions = ActionsBuilder()
                        .withActions(listOf(keyInputSource))
                        .withAdapter(EspressoW3CActionAdapter(uiController))
                        .build()

                actions.perform(params.sessionId!!)
                actions.release(params.sessionId!!)

                return null
            }
        }

        UiControllerPerformer(runnable).run()
    }

    override fun handleCompose(params: TextValueParams): Unit {
        try {
            val keys = params.value ?: return
            keys.forEach {
                getNodeInteractionById(params.elementId).performTextInput(it)
            }
        } catch (e: AssertionError) {
            throw StaleElementException(params.elementId!!)
        } catch (e: IllegalArgumentException) {
            throw InvalidElementStateException("Keys", params.elementId!!, e)
        }
    }
}
