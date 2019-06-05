package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.NotYetImplementedException
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
            // If it's a TEXT attribute, return the view's raw text
            if (it == ViewAttributesEnum.TEXT) {
                val viewInteraction = Element.getViewInteractionById(params.elementId)
                return ViewTextGetter().get(viewInteraction).rawText
            }

            val viewElement = ViewElement(Element.getViewById(params.elementId))
            when (it) {
                ViewAttributesEnum.CONTENT_DESC -> viewElement.contentDescription?.let { return it.toString() } ?: return null
                ViewAttributesEnum.CLASS -> return viewElement.className
                ViewAttributesEnum.CHECKABLE -> return java.lang.Boolean.toString(viewElement.isCheckable)
                ViewAttributesEnum.CHECKED -> return java.lang.Boolean.toString(viewElement.isChecked)
                ViewAttributesEnum.CLICKABLE -> return java.lang.Boolean.toString(viewElement.isClickable)
                ViewAttributesEnum.ENABLED -> return java.lang.Boolean.toString(viewElement.isEnabled)
                ViewAttributesEnum.FOCUSABLE -> return java.lang.Boolean.toString(viewElement.isFocusable)
                ViewAttributesEnum.FOCUSED -> return java.lang.Boolean.toString(viewElement.isFocused)
                ViewAttributesEnum.SCROLLABLE -> return java.lang.Boolean.toString(viewElement.isScrollable)
                ViewAttributesEnum.LONG_CLICKABLE -> return java.lang.Boolean.toString(viewElement.isLongClickable)
                ViewAttributesEnum.PASSWORD -> return java.lang.Boolean.toString(viewElement.isPassword)
                ViewAttributesEnum.SELECTED -> return java.lang.Boolean.toString(viewElement.isSelected)
                ViewAttributesEnum.VISIBLE -> return java.lang.Boolean.toString(viewElement.isVisible)
                ViewAttributesEnum.BOUNDS -> return viewElement.bounds.toShortString()
                ViewAttributesEnum.RESOURCE_ID -> return viewElement.resourceId
                ViewAttributesEnum.INDEX -> return Integer.toString(viewElement.index)
                ViewAttributesEnum.PACKAGE -> return viewElement.packageName
                ViewAttributesEnum.VIEW_TAG -> return viewElement.viewTag
                else -> throw NotYetImplementedException()
            }
        }

        // If we made it this far, we found no matching attribute. Throw an exception
        val supportedAttributeNames = ViewAttributesEnum.values().map { it.toString() }
        throw AppiumException(
                String.format("Attribute name should be one of %s. '%s' is given instead",
                        supportedAttributeNames, attributeName))
    }
}