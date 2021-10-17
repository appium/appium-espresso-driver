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

package io.appium.espressoserver.lib.drivers

import androidx.test.espresso.EspressoException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.LayoutAssertions
import io.appium.espressoserver.lib.handlers.exceptions.*
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.helpers.ViewFinder
import io.appium.espressoserver.lib.model.*
import io.appium.espressoserver.lib.viewaction.ViewGetter
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class EspressoDriver : AppDriver {
    override val name = DriverContext.StrategyType.ESPRESSO

    override fun findElement(params: Locator): BaseElement {
        val parentView = params.elementId?.let {
            ViewGetter().getView(EspressoElement.getViewInteractionById(it))
        }
        // Test the selector
        val view = ViewFinder.findBy(
            parentView,
            params.using ?: throw InvalidSelectorException("Locator strategy cannot be empty"),
            params.value ?: throw InvalidArgumentException()
        )
            ?: throw NoSuchElementException(
                String.format(
                    "Could not find espresso element with strategy %s and selector %s",
                    params.using, params.value
                )
            )

        // If we have a match, return success
        return EspressoElement(view)
    }

    override fun findElements(params: Locator): List<EspressoElement> {
        val parentView = params.elementId?.let {
            ViewGetter().getView(EspressoElement.getViewInteractionById(it))
        }

        // Return as list of Elements
        return ViewFinder.findAllBy(
            parentView,
            params.using ?: throw InvalidSelectorException("Locator strategy cannot be empty"),
            params.value ?: throw InvalidArgumentException()
        )
            .map { EspressoElement(it) }
    }

    override fun getAttribute(elementId: String, attributeType: ViewAttributesEnum): String? {
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
        when (attributeType) {
            ViewAttributesEnum.CONTENT_DESC -> return viewElementGetter().contentDescription?.toString()
            ViewAttributesEnum.CLASS -> return viewElementGetter().className
            ViewAttributesEnum.CHECKABLE -> return viewElementGetter().isCheckable.toString()
            ViewAttributesEnum.CHECKED -> return viewElementGetter().isChecked.toString()
            ViewAttributesEnum.CLICKABLE -> return viewElementGetter().isClickable.toString()
            ViewAttributesEnum.ENABLED -> return viewElementGetter().isEnabled.toString()
            ViewAttributesEnum.FOCUSABLE -> return viewElementGetter().isFocusable.toString()
            ViewAttributesEnum.FOCUSED -> return viewElementGetter().isFocused.toString()
            ViewAttributesEnum.SCROLLABLE -> return viewElementGetter().isScrollable.toString()
            ViewAttributesEnum.LONG_CLICKABLE -> return viewElementGetter().isLongClickable.toString()
            ViewAttributesEnum.PASSWORD -> return viewElementGetter().isPassword.toString()
            ViewAttributesEnum.SELECTED -> return viewElementGetter().isSelected.toString()
            ViewAttributesEnum.VISIBLE -> return uncheckedViewElementGetter().isVisible.toString()
            ViewAttributesEnum.BOUNDS -> return viewElementGetter().bounds.toShortString()
            ViewAttributesEnum.RESOURCE_ID -> return viewElementGetter().resourceId
            ViewAttributesEnum.INDEX -> return viewElementGetter().index.toString()
            ViewAttributesEnum.PACKAGE -> return viewElementGetter().packageName
            ViewAttributesEnum.VIEW_TAG -> return viewElementGetter().viewTag
            ViewAttributesEnum.NO_ELLIPSIZED_TEXT -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noEllipsizedText())
            }
            ViewAttributesEnum.NO_MULTILINE_BUTTONS -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noMultilineButtons())
            }
            ViewAttributesEnum.NO_OVERLAPS -> return checkToAttributeValue {
                viewInteractionGetter().check(LayoutAssertions.noOverlaps())
            }
            // If it's a TEXT attribute, return the view's raw text
            ViewAttributesEnum.TEXT -> return ViewTextGetter()[viewInteractionGetter()].rawText
            else -> throw NotYetImplementedException()
        }
    }
}