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

import java.util.ArrayList
import java.util.Collections

import androidx.test.espresso.UiController
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.Action
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionBuilder
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceBuilder
import io.appium.espressoserver.lib.model.TextParams
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_DOWN
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.ActionType.KEY_UP
import io.appium.espressoserver.lib.helpers.w3c.models.InputSource.InputSourceType.KEY

class Keys : RequestHandler<TextParams, Void?> {

    @Throws(AppiumException::class)
    override fun handle(params: TextParams): Void? {
        val runnable = UiControllerRunnable<Void> { uiController ->
            // Add a list of keyDown + keyUp actions for each key
            val keyActions = arrayListOf<Action>()
            params.value.forEach {
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

            actions.perform(params.sessionId)
            actions.release(params.sessionId)

            null
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
