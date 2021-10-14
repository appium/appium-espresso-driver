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

import android.widget.NumberPicker
import android.widget.ProgressBar
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.PerformException

import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.EspressoElement
import io.appium.espressoserver.lib.model.TextValueParams

import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.getNodeInteractionById
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class ElementValue(private val isReplacing: Boolean) : RequestHandler<TextValueParams, Unit> {

    override fun handleEspresso(params: TextValueParams): Unit {
        val value: String = extractTextToEnter(params)

        val elementId = params.elementId
        val view = EspressoElement.getViewById(elementId)

        try {
            if (view is ProgressBar) {
                view.progress = Integer.parseInt(value)
            }
            if (view is NumberPicker) {
                view.value = Integer.parseInt(value)
            }
        } catch (e: NumberFormatException) {
            throw InvalidArgumentException(String.format("Cannot convert '$value' to an integer"))
        }

        val viewInteraction = EspressoElement.getViewInteractionById(elementId)
        if (isReplacing) {
            viewInteraction.perform(replaceText(value))
        } else {
            try {
                viewInteraction.perform(typeText(value))
            } catch (e: PerformException) {
                throw InvalidElementStateException("sendKeys/setValueImmediate", params.elementId!!, e)
            } catch (e: RuntimeException) {
                e.message?.let {
                    if (!it.contains("IME does not understand how to translate")) throw e
                }
                AndroidLogger.debug("Trying replaceText action as a workaround to type the '$value' text into the input field")
                @Suppress("ReplaceGetOrSet") val currentText = ViewTextGetter().get(viewInteraction)
                if (currentText.rawText.isEmpty() || currentText.isHint) {
                    viewInteraction.perform(replaceText(value))
                } else {
                    AndroidLogger.debug("Current input field's text: '$currentText'")
                    viewInteraction.perform(replaceText(currentText.toString() + value))
                }
            }
        }
    }

    override fun handleCompose(params: TextValueParams): Unit {
        val value: String = extractTextToEnter(params)
        try {
            if (isReplacing) {
                getNodeInteractionById(params.elementId).performTextClearance()
            }
            getNodeInteractionById(params.elementId).performTextInput(value)
        } catch (e: IllegalArgumentException) {
            throw InvalidElementStateException("Clear", params.elementId!!, e)
        }
    }

    private fun extractTextToEnter(params: TextValueParams) =
        when (Pair(params.value == null, params.text == null)) {
            Pair(
                first = true,
                second = true
            ) -> throw InvalidArgumentException("Must provide 'value' or 'text' property")
            Pair(
                first = false,
                second = true
            ) -> params.value!!.joinToString(separator = "") // for MJSONWP
            else -> params.text!! // Prior W3C
        }
}
