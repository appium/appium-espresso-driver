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
        val value: String = when (Pair(params.value == null, params.text == null)) {
            Pair(first=true, second=true) -> throw InvalidArgumentException("Must provide 'value' or 'text' property")
            Pair(first=false, second=true) -> params.value!!.joinToString(separator="")
            Pair(first=true, second=false) -> params.text!!
            else -> params.value!!.joinToString() // for backward-compatibility
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