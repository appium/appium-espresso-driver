package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.model.AppiumParams
import io.appium.espressoserver.lib.model.Element

class ElementEquals : RequestHandler<AppiumParams, Boolean> {

    @Throws(AppiumException::class)
    override fun handle(params: AppiumParams): Boolean? {
        val elementId = params.elementId
        val otherElementId = params.getUriParameterValue("otherId")
        val viewOne = Element.getViewById(elementId)
        val viewTwo = Element.getViewById(otherElementId)
        return viewOne == viewTwo
    }
}