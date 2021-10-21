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
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.LayoutAssertions
import io.appium.espressoserver.EspressoServerRunnerTest
import io.appium.espressoserver.lib.drivers.DriverContext
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.getSemanticsNode
import io.appium.espressoserver.lib.model.*
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class GetAttribute : RequestHandler<AppiumParams, String?> {

    val espressoAttributes by lazy { EspressoAttributes() }

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): String? {
        val attributeName = params.getUriParameterValue("name")
        if (attributeName == null || attributeName.trim { it <= ' ' }.isEmpty()) {
            throw AppiumException("Attribute name cannot be null or empty")
        }

        return when (EspressoServerRunnerTest.context.currentStrategyType) {
            DriverContext.StrategyType.COMPOSE -> getComposeAttribute(params.elementId!!, attributeName)
            DriverContext.StrategyType.ESPRESSO -> getEspressoAttribute(params.elementId!!, attributeName)
        }
    }


    private fun getComposeAttribute(elementId: String, attributeName: String): String? {
        return ComposeNodeElement(getSemanticsNode(elementId)).getAttribute(attributeName)
    }

    private fun getEspressoAttribute(elementId: String, attributeName: String): String? {
        val viewElementGetter: () -> ViewElement =
            { ViewElement(EspressoElement.getViewById(elementId)) }
        val uncheckedViewElementGetter: () -> ViewElement =
            { ViewElement(EspressoElement.getViewById(elementId, false)) }
        val viewInteractionGetter: () -> ViewInteraction =
            { EspressoElement.getViewInteractionById(elementId) }
        val checkToAttributeValue: (() -> Unit) -> String = {
            try {
                it()
                "true"
            } catch (e: Exception) {
                if (e is EspressoException) {
                    e.message?.let { msg -> AndroidLogger.info(msg) }
                    "false"
                } else {
                    throw e
                }
            }
        }
        when (espressoAttributes.valueOf(attributeName)) {
            AttributesEnum.CONTENT_DESC -> return viewElementGetter().contentDescription?.toString()
            AttributesEnum.CLASS -> return viewElementGetter().className
            AttributesEnum.CHECKABLE -> return viewElementGetter().isCheckable.toString()
            AttributesEnum.CHECKED -> return viewElementGetter().isChecked.toString()
            AttributesEnum.CLICKABLE -> return viewElementGetter().isClickable.toString()
            AttributesEnum.ENABLED -> return viewElementGetter().isEnabled.toString()
            AttributesEnum.FOCUSABLE -> return viewElementGetter().isFocusable.toString()
            AttributesEnum.FOCUSED -> return viewElementGetter().isFocused.toString()
            AttributesEnum.SCROLLABLE -> return viewElementGetter().isScrollable.toString()
            AttributesEnum.LONG_CLICKABLE -> return viewElementGetter().isLongClickable.toString()
            AttributesEnum.PASSWORD -> return viewElementGetter().isPassword.toString()
            AttributesEnum.SELECTED -> return viewElementGetter().isSelected.toString()
            AttributesEnum.VISIBLE -> return uncheckedViewElementGetter().isVisible.toString()
            AttributesEnum.BOUNDS -> return viewElementGetter().bounds.toShortString()
            AttributesEnum.RESOURCE_ID -> return viewElementGetter().resourceId
            AttributesEnum.INDEX -> return viewElementGetter().index.toString()
            AttributesEnum.PACKAGE -> return viewElementGetter().packageName
            AttributesEnum.VIEW_TAG -> return viewElementGetter().viewTag
            AttributesEnum.NO_ELLIPSIZED_TEXT -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noEllipsizedText())
            }
            AttributesEnum.NO_MULTILINE_BUTTONS -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noMultilineButtons())
            }
            AttributesEnum.NO_OVERLAPS -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noOverlaps())
            }
            // If it's a TEXT attribute, return the view's raw text
            AttributesEnum.TEXT -> return ViewTextGetter()[viewInteractionGetter()].rawText
            else -> throw NotYetImplementedException(
                "Espresso doesn't support attribute '$attributeName', Attribute name should be one of ${composeAttributes.supportedAttributes()}\"")
        }
    }
}
