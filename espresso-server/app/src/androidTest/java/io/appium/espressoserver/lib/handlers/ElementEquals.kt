package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.EspressoElement

class ElementEquals : RequestHandler<AppiumParams, Boolean> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: AppiumParams): Boolean {
        val elementId = params.elementId
        val otherElementId = params.getUriParameterValue("otherId")
                ?: throw InvalidArgumentException("'otherElementId' query parameter not found")
        val viewOne = EspressoElement.getCachedViewStateById(elementId).view
        val viewTwo = EspressoElement.getCachedViewStateById(otherElementId).view
        return viewOne == viewTwo
    }
}