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

import androidx.test.espresso.PerformException
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidElementStateException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.TextParams
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import io.appium.espressoserver.lib.helpers.AndroidLogger.logger

class SendKeys : RequestHandler<TextParams, Void?> {

    @Throws(AppiumException::class)
    override fun handle(params: TextParams): Void? {
        val id = params.elementId
        val view = Element.getViewById(id)

        // Convert the array of text to a String
        var value: String? = params.value.joinToString(separator="")

        try {
            if (view is ProgressBar) {
                view.progress = Integer.parseInt(value!!)
                return null
            }
            if (view is NumberPicker) {
                view.value = Integer.parseInt(value!!)
                return null
            }
        } catch (e: NumberFormatException) {
            throw InvalidArgumentException("Cannot convert '${value}' to an integer")
        }

        val viewInteraction = Element.getViewInteractionById(id)
        try {
            viewInteraction.perform(typeText(value!!))
        } catch (e: PerformException) {
            throw InvalidElementStateException("sendKeys", params.elementId, e)
        } catch (e: RuntimeException) {
            e.message?.let {
                it.contains("IME does not understand how to translate") ?: throw e
            }
            params.text?.let {
                value = it
            }
            logger.debug("Trying replaceText action as a workaround to type the '${value}' text into the input field")
            val currentText = ViewTextGetter().get(viewInteraction)
            if (currentText.text.isEmpty() || currentText.isHint) {
                viewInteraction.perform(replaceText(value))
            } else {
                logger.debug("Current input field's text: '${currentText}'")
                viewInteraction.perform(replaceText(currentText.toString() + value))
            }
        }

        return null
    }
}
