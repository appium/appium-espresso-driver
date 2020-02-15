package io.appium.espressoserver.lib.handlers

import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.LayoutAssertions.*
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
import io.appium.espressoserver.lib.helpers.AndroidLogger
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.ViewAttributesEnum
import io.appium.espressoserver.lib.model.ViewElement
import io.appium.espressoserver.lib.viewaction.ViewTextGetter

class GetAttribute : RequestHandler<AppiumParams, String?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): String? {
        val attributeName = params.getUriParameterValue("name")
        if (attributeName == null || attributeName.trim { it <= ' ' }.isEmpty()) {
            throw AppiumException("Attribute name cannot be null or empty")
        }

        // Map attributeName to ENUM attribute
        ViewAttributesEnum.values().find {
            attributeName.equals(it.toString(), ignoreCase = true)
        }?.let {
            val viewElementGetter: () -> ViewElement = { ViewElement(Element.getViewById(params.elementId)) }
            val viewInteractionGetter: () -> ViewInteraction = { Element.getViewInteractionById(params.elementId) }
            val checkToAttributeValue: (() -> Unit) -> String = {
                try {
                    it()
                    "true"
                } catch (e: Exception) {
                    e.message?.let { msg -> AndroidLogger.logger.info(msg) }
                    "false"
                }
            }
            when (it) {
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
                ViewAttributesEnum.VISIBLE -> return viewElementGetter().isVisible.toString()
                ViewAttributesEnum.BOUNDS -> return viewElementGetter().bounds.toShortString()
                ViewAttributesEnum.RESOURCE_ID -> return viewElementGetter().resourceId
                ViewAttributesEnum.INDEX -> return viewElementGetter().index.toString()
                ViewAttributesEnum.PACKAGE -> return viewElementGetter().packageName
                ViewAttributesEnum.VIEW_TAG -> return viewElementGetter().viewTag
                ViewAttributesEnum.NO_ELLIPSIZED_TEXT -> return checkToAttributeValue {
                    viewInteractionGetter().check(noEllipsizedText())
                }
                ViewAttributesEnum.NO_MULTILINE_BUTTONS -> return checkToAttributeValue {
                    viewInteractionGetter().check(noMultilineButtons())
                }
                ViewAttributesEnum.NO_OVERLAPS -> return checkToAttributeValue {
                    viewInteractionGetter().check(noOverlaps())
                }
                // If it's a TEXT attribute, return the view's raw text
                ViewAttributesEnum.TEXT -> return ViewTextGetter().get(viewInteractionGetter()).rawText
                else -> throw NotYetImplementedException()
            }
        }

        // If we made it this far, we found no matching attribute. Throw an exception
        val supportedAttributeNames = ViewAttributesEnum.values().map { it.toString() }
        throw AppiumException("Attribute name should be one of $supportedAttributeNames. " +
                "'$attributeName' is given instead")
    }
}
