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

package io.appium.espressoserver.lib.helpers

import android.view.inputmethod.EditorInfo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.viewaction.ViewGetter

object IMEHelpers {
    private val ACTION_CODES_MAP = mapOf(
            "normal" to 0,
            "unspecified" to 0,
            "none" to 1,
            "go" to 2,
            "search" to 3,
            "send" to 4,
            "next" to 5,
            "done" to 6,
            "previous" to 7
    )

    @Throws(AppiumException::class)
    private fun toActionCode(action: Any): Int {
        if (action is Long) {
            return action.toInt()
        }
        if (action is String) {
            return ACTION_CODES_MAP[action.toLowerCase()]
                    ?: throw InvalidArgumentException(
                            "The action value can be one of ${ACTION_CODES_MAP.keys}. '$action' is given instead")
        }
        throw InvalidArgumentException(
                "The action value can be either an integer action code or one of ${ACTION_CODES_MAP.keys}. " +
                        "'$action' is given instead")
    }

    @Throws(AppiumException::class)
    fun performEditorAction(action: Any?) {
        val viewInteraction: ViewInteraction
        try {
            viewInteraction = onView(hasFocus())
        } catch (e: NoMatchingViewException) {
            throw InvalidElementStateException(
                    "Currently there is no focused element to perform ${action
                            ?: "the default"} editor action on", e)
        }

        if (action == null) {
            AndroidLogger.debug("Performing the default editor action on the focused element")
            try {
                viewInteraction.perform(pressImeActionButton())
                return
            } catch (e: PerformException) {
                throw InvalidElementStateException("Cannot perform the default action on the focused element")
            }
        }

        val actionCode = toActionCode(action)
        AndroidLogger.debug("Performing editor action $actionCode on the focused element")
        val view = ViewGetter().getView(viewInteraction)
        val ic = view.onCreateInputConnection(EditorInfo())
        if (!ic.performEditorAction(actionCode)) {
            throw InvalidElementStateException("Cannot perform editor action $action on the focused element")
        }
    }
}
