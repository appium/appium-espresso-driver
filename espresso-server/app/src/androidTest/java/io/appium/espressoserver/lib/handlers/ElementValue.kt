package io.appium.espressoserver.lib.handlers

import android.widget.NumberPicker
import android.widget.ProgressBar

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.model.Element
import io.appium.espressoserver.lib.model.ElementValueParams

import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText

class ElementValue(private val isReplacing: Boolean) : RequestHandler<ElementValueParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: ElementValueParams): Void? {
        val value = if (params.value != null) {
            params.value!!.joinToString()
        } else {
            throw InvalidArgumentException("Must provide 'value' property")
        }


        val elementId = params.elementId
        val view = Element.getViewById(elementId)

        try {
            if (view is ProgressBar) {
                view.progress = Integer.parseInt(value)
                return null
            }
            if (view is NumberPicker) {
                view.value = Integer.parseInt(value)
                return null
            }
        } catch (e: NumberFormatException) {
            throw InvalidArgumentException(String.format("Cannot convert '%s' to an integer",
                    params.value))
        }

        val viewInteraction = Element.getViewInteractionById(elementId)
        if (isReplacing) {
            viewInteraction.perform(replaceText(value))
        } else {
            viewInteraction.perform(typeText(value))
        }
        return null
    }
}