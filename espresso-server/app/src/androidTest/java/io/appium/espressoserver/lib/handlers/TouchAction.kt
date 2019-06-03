package io.appium.espressoserver.lib.handlers

import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.helpers.w3c.adapter.espresso.EspressoW3CActionAdapter
import io.appium.espressoserver.lib.helpers.w3c.models.Actions.ActionsBuilder
import io.appium.espressoserver.lib.model.TouchAction.Companion.toW3CInputSources
import io.appium.espressoserver.lib.model.TouchActionsParams
import io.appium.espressoserver.lib.viewaction.UiControllerPerformer
import io.appium.espressoserver.lib.viewaction.UiControllerRunnable

class TouchAction : RequestHandler<TouchActionsParams, Void?> {

    @Throws(AppiumException::class)
    override fun handleInternal(params: TouchActionsParams): Void? {
        val runnable = UiControllerRunnable<Void> { uiController ->
            val inputSources = toW3CInputSources(listOf(params.actions))
            val actions = ActionsBuilder()
                    .withAdapter(EspressoW3CActionAdapter(uiController))
                    .withActions(inputSources)
                    .build()
            actions.perform(params.sessionId)
            actions.release(params.sessionId)

            null
        }

        UiControllerPerformer(runnable).run()
        return null
    }
}
